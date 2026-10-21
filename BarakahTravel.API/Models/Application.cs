using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class Application
{
    [Key]
    [Column("ApplicationID")]
    public int ApplicationId { get; set; }

    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [Column("PackageID")]
    public int PackageId { get; set; }

    [StringLength(30)]
    public string ApplicationStatus { get; set; } = null!;

    public DateTime? SubmittedAt { get; set; }

    public DateTime? ReviewedAt { get; set; }

    public int? ReviewedBy { get; set; }

    [StringLength(1000)]
    public string? Notes { get; set; }

    public DateTime CreatedAt { get; set; }

    [InverseProperty("Application")]
    public virtual ICollection<Booking> Bookings { get; set; } = new List<Booking>();

    [ForeignKey("CustomerId")]
    [InverseProperty("Applications")]
    public virtual Customer Customer { get; set; } = null!;

    [InverseProperty("Application")]
    public virtual ICollection<Document> Documents { get; set; } = new List<Document>();

    [ForeignKey("PackageId")]
    [InverseProperty("Applications")]
    public virtual Package Package { get; set; } = null!;

    [ForeignKey("ReviewedBy")]
    [InverseProperty("Applications")]
    public virtual User? ReviewedByNavigation { get; set; }

    [InverseProperty("Application")]
    public virtual ICollection<TravelDetail> TravelDetails { get; set; } = new List<TravelDetail>();
}
