package com.example.ghostmappers.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.LightBlue
import com.example.ghostmappers.ui.theme.Maron

@Composable
fun GhostCharacteristic(
    icon: Int,
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var showExistingSelected by remember { mutableStateOf(true) }
    val isStatus = items.any { it.contains("Peaceful", ignoreCase = true) }

    val canEdit = (selectedItem == "" || !showExistingSelected || isStatus)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Beige)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "Icon",
                    modifier = Modifier.size(20.dp),
                    tint = LightBlue,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
        ) {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = {},
                readOnly = true,
                enabled = canEdit,
                trailingIcon = {
                    if (canEdit) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (expanded) "Hide dropdown" else "Show dropdown",

                            modifier = Modifier.clickable { expanded = !expanded },
                            tint = Maron
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()

                    .clickable(enabled = canEdit) { expanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,

                    disabledTextColor = Color.Black,
                    disabledBorderColor = Maron,
                    disabledContainerColor = Color.Transparent,

                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Maron,
                    focusedBorderColor = Maron
                )
            )
            
            if (canEdit) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                        .background(Color.White),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                ) {
                    items.forEach { itemLabel ->
                        DropdownMenuItem(
                            text = { Text(itemLabel) },
                            onClick = {
                                onItemSelected(itemLabel)
                                showExistingSelected = false
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewGhostCharacteristic() {
    val items = listOf("Item 1", "Item 2", "Item 3")
    GhostCharacteristic(R.drawable.heartbeat, "Label", items, "", onItemSelected = {})
}