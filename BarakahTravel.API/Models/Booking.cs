using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

[Index("BookingReference", Name = "UQ__Bookings__F9B66F617AB3E034", IsUnique = true)]
public partial class Booking
{
    [Key]
    [Column("BookingID")]
    public int BookingId { get; set; }

    [Column("ApplicationID")]
    public int ApplicationId { get; set; }

    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [Column("PackageID")]
    public int PackageId { get; set; }

    [StringLength(50)]
    public string BookingReference { get; set; } = null!;

    [StringLength(30)]
    public string BookingStatus { get; set; } = null!;

    public DateOnly BookingDate { get; set; }

    [Column(TypeName = "decimal(12, 2)")]
    public decimal TotalAmount { get; set; }

    [StringLength(500)]
    public string? Notes { get; set; }

    [ForeignKey("ApplicationId")]
    [InverseProperty("Bookings")]
    public virtual Application Application { get; set; } = null!;

    [ForeignKey("CustomerId")]
    [InverseProperty("Bookings")]
    public virtual Customer Customer { get; set; } = null!;

    [ForeignKey("PackageId")]
    [InverseProperty("Bookings")]
    public virtual Package Package { get; set; } = null!;

    [InverseProperty("Booking")]
    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();

    [InverseProperty("Booking")]
    public virtual ICollection<TravelSchedule> TravelSchedules { get; set; } = new List<TravelSchedule>();
}
