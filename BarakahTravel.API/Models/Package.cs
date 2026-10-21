using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class Package
{
    [Key]
    [Column("PackageID")]
    public int PackageId { get; set; }

    [StringLength(100)]
    public string PackageName { get; set; } = null!;

    [StringLength(500)]
    public string? Description { get; set; }

    [Column(TypeName = "decimal(12, 2)")]
    public decimal Price { get; set; }

    public int DurationDays { get; set; }

    public DateOnly? DepartureDate { get; set; }

    public DateOnly? ReturnDate { get; set; }

    public bool IsActive { get; set; }

    public DateTime CreatedAt { get; set; }

    [InverseProperty("Package")]
    public virtual ICollection<Application> Applications { get; set; } = new List<Application>();

    [InverseProperty("Package")]
    public virtual ICollection<Booking> Bookings { get; set; } = new List<Booking>();
}
