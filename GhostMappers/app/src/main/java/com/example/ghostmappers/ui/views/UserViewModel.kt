package com.example.ghostmappers.ui.views

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghostmappers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val numReports: Int = 0,
    val numKills: Int = 0,
    val ghostTypesSeen: List<String> = emptyList(),
    val ghostTypeCount: Map<String, Long> = emptyMap(),
    val notifications : Boolean = false,
    val sound : Boolean = false,
    val vibration : Boolean = false,
    val ghostAlerts : Boolean = false
)

sealed class ProfileImage {
    data class Uploaded(val url: String) : ProfileImage()
    data class Preset(val resId: Int) : ProfileImage()
}

class UserViewModel : ViewModel() {

    private val _profileImage = mutableStateOf<ProfileImage>(ProfileImage.Preset(R.drawable.halloween_ghost))
    val profileImage: State<ProfileImage> = _profileImage

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val _userProfile = mutableStateOf(UserProfile())
    val userProfile: State<UserProfile> = _userProfile

    private val _isLoading = mutableStateOf(false)

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            loadUserProfile()
        }
    }

    fun loadUserProfile(onComplete: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val username = doc.getString("username") ?: ""
                    val email = doc.getString("email") ?: ""
                    val numReports = doc.getLong("numReports")?.toInt() ?: 0
                    val numKills = doc.getLong("numKills")?.toInt() ?: 0
                    val ghostTypesSeen = doc.get("ghostTypesSeen") as? List<String> ?: emptyList()
                    val ghostTypeCount =
                        doc.get("ghostTypeCount") as? Map<String, Long> ?: emptyMap()

                    val profileUrl = doc.getString("profileImageUrl")
                    val presetId = doc.getLong("presetResId")?.toInt()
                    if (!profileUrl.isNullOrEmpty()) {
                        _profileImage.value = ProfileImage.Uploaded(profileUrl)
                    } else if (presetId != null && presetId != 0) {
                        _profileImage.value = ProfileImage.Preset(presetId)
                    } else {
                        _profileImage.value = ProfileImage.Preset(R.drawable.halloween_ghost)
                    }
                    val notifications = doc.getBoolean("notifications") ?: false
                    val sound = doc.getBoolean("sound") ?: false
                    val vibration = doc.getBoolean("vibration") ?: false
                    val ghostAlerts = doc.getBoolean("ghostAlerts") ?: false


                    // Update state directly
                    _userProfile.value = UserProfile(
                        username = username,
                        email = email,
                        numReports = numReports,
                        numKills = numKills,
                        ghostTypesSeen = ghostTypesSeen,
                        ghostTypeCount = ghostTypeCount,
                        notifications = notifications,
                        sound = sound,
                        vibration = vibration,
                        ghostAlerts = ghostAlerts
                    )
                    _isLoading.value = false
                    onComplete()
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    onComplete()
                }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                db.collection("users").document(uid)
                    .update("profileImageUrl", url.toString())

                _profileImage.value = ProfileImage.Uploaded(url.toString())
            }
    }

    fun updateGhostAlerts(ghostAlerts : Boolean) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            db.collection("users").document(uid).update("ghostAlerts", ghostAlerts)
        }
    }

    fun selectPreset(resId: Int) {
        val uid = auth.currentUser?.uid ?: return

        _profileImage.value = ProfileImage.Preset(resId)

        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update("profileImageUrl", "")
                    .await()

                db.collection("users").document(uid)
                    .update("presetResId", resId)
                    .await()
            } catch (e: Exception) {

            }
        }
    }


}
