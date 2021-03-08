package com.example.p2pchat.network;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class Interrogator {

    private final int PERIOD_MS = 3000;
    private Observable<Boolean> observable;

    public Interrogator(String fromPublicKey, String fromName) {

        observable = Observable.create(emmit -> {

            while (true) {
                //TODO: check if it will work if create it just once
                MCSender sender = new MCSender("all", fromPublicKey, fromName);

                sender.getObservable().subscribe(new CompletableObserver() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //do nothing
                    }

                    @Override
                    public void onComplete() {
                        emmit.onNext(true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        emmit.onError(e);
                    }
                });

                try {
                    Sleep();
                } catch (Exception e) {
                    emmit.onError(e);
                    break;
                }
            }

            emmit.onComplete();
        });
    }

    public Observable<Boolean> getObservable() {
        return observable;
    }

    private void Sleep() throws InterruptedException {
        //TODO: come up with smth better...
        Thread.sleep(PERIOD_MS);
    }

}
