package com.sngtech.signconnect.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryModel {

    public static void addItemtoHistory(FirebaseUser user, FirebaseFirestore db, HistoryItem historyItem) {
        db.collection("history").document(user.getUid()).set(new HashMap<String, Object>(), SetOptions.merge());

        DocumentReference docRef = db.collection("history").document(user.getUid());
        docRef.update("historyItems", FieldValue.arrayUnion(historyItem)).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.println(Log.INFO, "test_firestore", "Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.INFO, "test_firestore", "Failed: " + e.getMessage());
            }
        });
    }

    public static void queryHistoryItems(FirebaseUser user, FirebaseFirestore db, HistoryQueryListener listener) {
        db.collection("history").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<Object> array = (ArrayList<Object>) documentSnapshot.get("historyItems");
                if(array == null)
                    return;

                List<HistoryItem> queriedItems = new ArrayList<>();

                for(Object obj : array) {
                    Map<String, Object> field = (Map<String, Object>) obj;
                    String result = (String) field.get("result");
                    HistoryItem.SignType signType = HistoryItem.SignType.valueOf((String) field.get("signType"));
                    String dataTimeLearnt = (String) field.get("dateTimeLearnt");
                    String capturedPath = (String) field.get("capturedPath");
                    long facing = (long) field.get("facing");

                    HistoryItem item = new HistoryItem(result, dataTimeLearnt, signType, capturedPath);
                    item.setFacing((int) facing);
                    queriedItems.add(item);
                }
                listener.onQuerySuccess(queriedItems);
                Log.println(Log.INFO, "test_firestore", "Success reading");
            }
        }).addOnFailureListener(e -> {
            Log.println(Log.INFO, "test_firestore", "Failed to read: " + e.getMessage());
        });
    }
}
