package com.hotel.patterns.observer;
import java.util.*;
public class BookingNotifier {
    private final List<BookingObserver> observers=new ArrayList<>();
    public void subscribe(BookingObserver observer) { observers.add(observer); }
    public void notifyAllObservers(String message) { observers.forEach(o -> o.onBookingChanged(message)); }
}
