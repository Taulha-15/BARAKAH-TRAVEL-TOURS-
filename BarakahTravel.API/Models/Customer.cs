using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class Customer
{
    [Key]
    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [StringLength(50)]
    public string FirstName { get; set; } = null!;

    [StringLength(50)]
    public string Surname { get; set; } = null!;

    [Column("IDNumber")]
    [StringLength(30)]
    public string? Idnumber { get; set; }

    [StringLength(30)]
    public string? PassportNumber { get; set; }

    public DateOnly? DateOfBirth { get; set; }

    [StringLength(20)]
    public string? Gender { get; set; }

    [StringLength(30)]
    public string Phone { get; set; } = null!;

    [StringLength(100)]
    public string? Email { get; set; }

    [StringLength(255)]
    public string? Address { get; set; }

    public DateTime CreatedAt { get; set; }

    [InverseProperty("Customer")]
    public virtual ICollection<Application> Applications { get; set; } = new List<Application>();

    [InverseProperty("Customer")]
    public virtual ICollection<Booking> Bookings { get; set; } = new List<Booking>();

    [InverseProperty("Customer")]
    public virtual CustomerAccount? CustomerAccount { get; set; }

    [InverseProperty("Customer")]
    public virtual ICollection<CustomerRegistrationCode> CustomerRegistrationCodes { get; set; } = new List<CustomerRegistrationCode>();

    [InverseProperty("Customer")]
    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();

    public string Status { get; set; } = "Pending";
}
