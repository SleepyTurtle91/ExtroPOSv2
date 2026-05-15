# Hotel & Homestay Management Module

A specialized module for hospitality businesses integrated into the POS ecosystem.

## 1. Data Models

- **Room**: Defines the unit of stay (Name, Type, Price, Status).
- **Booking**: Links a guest to a room for a specific duration.
- **Guest**: Centralized guest profile with identity and loyalty data.
- **HotelAddon**: Links non-room charges (meals, tours) to a booking.

## 2. Room Lifecycle

Rooms move through several statuses:
- `AVAILABLE`: Ready for booking.
- `OCCUPIED`: Guest has checked in.
- `DIRTY`: Needs cleaning after check-out.
- `MAINTENANCE`: Out of service for repairs.

## 3. UI Features

### Hotel Dashboard
- View real-time room availability.
- Summary cards for daily bookings and expected check-ins.

### Booking Flow
1. Check availability for dates.
2. Create/Select Guest.
3. Confirm Booking.
4. Check-in (Change status to `OCCUPIED`).
5. Add charges/addons.
6. Check-out (Generate Final Bill & change status to `DIRTY`).

## 4. Implementation Details

- **Database**: 4 new tables (`hotel_rooms`, `hotel_bookings`, `hotel_guests`, `hotel_addons`).
- **ViewModel**: `HotelViewModel` handles the temporal logic (Start/End of day).
