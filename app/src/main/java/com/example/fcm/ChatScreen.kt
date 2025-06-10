package com.example.fcm

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.reflect.Modifier

@Composable
fun ChatScreen(
    messageText:String,
    onMessageChange:(String)->Unit,
    onMessageSend:()->Unit,
    onMessageBroadcast:()->Unit
){
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value =messageText ,
            onValueChange = onMessageChange,
            placeholder = { Text(text = "Enter a message")},
            modifier = androidx.compose.ui.Modifier
                .padding(16.dp)
                .fillMaxWidth())
        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {

            IconButton(onClick = onMessageSend) {
                Icon(imageVector =Icons.Default.Send, contentDescription = "Send" )
            }

            Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))

            IconButton(onClick = onMessageBroadcast) {
                Icon(imageVector =Icons.Default.Share, contentDescription = "BroadCast" )
            }
        }
    }
}