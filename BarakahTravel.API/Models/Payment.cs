using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class Payment
{
    [Key]
    [Column("PaymentID")]
    public int PaymentId { get; set; }

    [Column("BookingID")]
    public int BookingId { get; set; }

    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [Column(TypeName = "decimal(12, 2)")]
    public decimal Amount { get; set; }

    public DateOnly PaymentDate { get; set; }

    [StringLength(50)]
    public string? PaymentMethod { get; set; }

    [StringLength(100)]
    public string? ReferenceNumber { get; set; }

    [StringLength(30)]
    public string PaymentStatus { get; set; } = null!;

    public int RecordedBy { get; set; }

    [StringLength(500)]
    public string? Notes { get; set; }

    [ForeignKey("BookingId")]
    [InverseProperty("Payments")]
    public virtual Booking Booking { get; set; } = null!;

    [ForeignKey("CustomerId")]
    [InverseProperty("Payments")]
    public virtual Customer Customer { get; set; } = null!;

    [ForeignKey("RecordedBy")]
    [InverseProperty("Payments")]
    public virtual User RecordedByNavigation { get; set; } = null!;
}
