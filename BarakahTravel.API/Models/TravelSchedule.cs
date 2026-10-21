using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class TravelSchedule
{
    [Key]
    [Column("ScheduleID")]
    public int ScheduleId { get; set; }

    [Column("BookingID")]
    public int BookingId { get; set; }

    public DateOnly DepartureDate { get; set; }

    public DateOnly ReturnDate { get; set; }

    public TimeOnly? DepartureTime { get; set; }

    public TimeOnly? ReturnTime { get; set; }

    [StringLength(100)]
    public string? DepartureAirport { get; set; }

    [StringLength(100)]
    public string? ArrivalAirport { get; set; }

    [StringLength(100)]
    public string? Airline { get; set; }

    [StringLength(50)]
    public string? FlightNumber { get; set; }

    [StringLength(30)]
    public string ScheduleStatus { get; set; } = null!;

    [StringLength(500)]
    public string? Notes { get; set; }

    [ForeignKey("BookingId")]
    [InverseProperty("TravelSchedules")]
    public virtual Booking Booking { get; set; } = null!;
}
