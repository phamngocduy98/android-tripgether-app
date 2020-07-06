package cf.bautroixa.maptest.presenter.impl;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.objects.Trip;
import cf.bautroixa.maptest.presenter.TripInvitationQrPresenter;
import cf.bautroixa.maptest.utils.calculation.NumberGenerator;

public class TripInvitationQrPresenterImpl implements TripInvitationQrPresenter {
    ModelManager manager;
    View view;
    Fragment fragment;

    public TripInvitationQrPresenterImpl(View view, Fragment fragment) {
        this.manager = ModelManager.getInstance(fragment.requireContext());
        this.view = view;
        this.fragment = fragment;
    }

    @Override
    public void requestNewQR() {
        view.onLoading();
        manager.getCurrentTrip().attachListener(fragment, new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@NonNull Trip trip) {
                if (trip.isAvailable()) {
                    manager.getCurrentTrip().removeOnNewValueListener(this);
                    if (manager.isTripLeader()) {
                        generateJoinCode();
                    } else {
                        view.updateQR(manager.getCurrentTrip().getId(), null);
                    }
                }
            }
        });
    }

    public void generateJoinCode() {
        String newJoinCode = NumberGenerator.generateNumberString(12);
        manager.getCurrentTrip().sendUpdate(null, Trip.JOIN_CODE_VALUE, newJoinCode, Trip.JOIN_CODE_CREATE_TIME, FieldValue.serverTimestamp()).addOnCompleteListener(fragment.requireActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    view.updateQR(manager.getCurrentTrip().getId(), newJoinCode);
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }
}
