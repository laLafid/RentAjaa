package com.lafid.rentaja.models;

import com.google.firebase.Timestamp;

public class Booking {
    private String id;
    private String vehicleId;
    private String vehicleName;
    private String vehicleCategory;
    private String ownerId;
    private String ownerName;
    private String renterId;
    private String renterName;
    private String renterEmail;
    private String renterPhone;
    private String address;
    private String startDate;     // "yyyy-MM-dd"
    private String endDate;       // "yyyy-MM-dd"
    private int    days;
    private long   rentalCost;
    private long   deposit;
    private long   total;
    private String paymentMethod; // transfer, cash, ewallet
    private String status;        // pending, approved, rejected, active, completed
    private Timestamp createdAt;

    // Firestore requires empty constructor
    public Booking() {}

    // Status helpers
    public boolean isPending()   { return "pending".equals(status); }
    public boolean isApproved()  { return "approved".equals(status); }
    public boolean isRejected()  { return "rejected".equals(status); }
    public boolean isActive()    { return "active".equals(status); }
    public boolean isCompleted() { return "completed".equals(status); }

    public String getStatusLabel() {
        if (status == null) return "-";
        switch (status) {
            case "pending":   return "Menunggu";
            case "approved":  return "Disetujui";
            case "rejected":  return "Ditolak";
            case "active":    return "Aktif";
            case "completed": return "Selesai";
            default:          return status;
        }
    }

    // Getters & setters
    public String getId()                      { return id; }
    public void   setId(String id)             { this.id = id; }

    public String getVehicleId()               { return vehicleId; }
    public void   setVehicleId(String v)       { this.vehicleId = v; }

    public String getVehicleName()             { return vehicleName; }
    public void   setVehicleName(String v)     { this.vehicleName = v; }

    public String getVehicleCategory()         { return vehicleCategory; }
    public void   setVehicleCategory(String v) { this.vehicleCategory = v; }

    public String getOwnerId()                 { return ownerId; }
    public void   setOwnerId(String o)         { this.ownerId = o; }

    public String getOwnerName()               { return ownerName; }
    public void   setOwnerName(String o)       { this.ownerName = o; }

    public String getRenterId()                { return renterId; }
    public void   setRenterId(String r)        { this.renterId = r; }

    public String getRenterName()              { return renterName; }
    public void   setRenterName(String r)      { this.renterName = r; }

    public String getRenterEmail()             { return renterEmail; }
    public void   setRenterEmail(String r)     { this.renterEmail = r; }

    public String getRenterPhone()             { return renterPhone; }
    public void   setRenterPhone(String r)     { this.renterPhone = r; }

    public String getAddress()                 { return address; }
    public void   setAddress(String a)         { this.address = a; }

    public String getStartDate()               { return startDate; }
    public void   setStartDate(String s)       { this.startDate = s; }

    public String getEndDate()                 { return endDate; }
    public void   setEndDate(String e)         { this.endDate = e; }

    public int    getDays()                    { return days; }
    public void   setDays(int d)               { this.days = d; }

    public long   getRentalCost()              { return rentalCost; }
    public void   setRentalCost(long r)        { this.rentalCost = r; }

    public long   getDeposit()                 { return deposit; }
    public void   setDeposit(long d)           { this.deposit = d; }

    public long   getTotal()                   { return total; }
    public void   setTotal(long t)             { this.total = t; }

    public String getPaymentMethod()           { return paymentMethod; }
    public void   setPaymentMethod(String p)   { this.paymentMethod = p; }

    public String getStatus()                  { return status; }
    public void   setStatus(String s)          { this.status = s; }

    public Timestamp getCreatedAt()            { return createdAt; }
    public void      setCreatedAt(Timestamp t) { this.createdAt = t; }
}
