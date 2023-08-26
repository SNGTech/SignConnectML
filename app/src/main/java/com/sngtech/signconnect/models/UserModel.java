package com.sngtech.signconnect.models;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class UserModel {

    public static void addUser(FirebaseUser user, FirebaseFirestore db, User userObj) {
        db.collection("user").document(user.getUid()).set(new HashMap<String, Object>(), SetOptions.merge());

        db.collection("user").document(user.getUid()).set(userObj);
    }

    public static void queryUser(FirebaseUser user, FirebaseFirestore db, UserQueryListener listener) {
        DocumentReference docRef = db.collection("user").document(user.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                listener.onQuerySuccess(user);
            }
        });
    }
}
