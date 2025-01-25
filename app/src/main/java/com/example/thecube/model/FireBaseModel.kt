package com.example.thecube.model

import com.google.firebase.Firebase

import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FireBaseModel {
    private val db = Firebase.firestore


    init {
        val setting = firestoreSettings {
          setLocalCacheSettings(memoryCacheSettings {

          })
           }
        db.firestoreSettings = setting
        }


}