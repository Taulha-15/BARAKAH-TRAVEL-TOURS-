using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

[Index("Email", Name = "UQ__Users__A9D10534D0671077", IsUnique = true)]
public partial class User
{
    [Key]
    [Column("UserID")]
    public int UserId { get; set; }

    [StringLength(50)]
    public string FirstName { get; set; } = null!;

    [StringLength(50)]
    public string Surname { get; set; } = null!;

    [StringLength(100)]
    public string Email { get; set; } = null!;

    [StringLength(128)]
    public string? FirebaseUid { get; set; }

    [StringLength(255)]
    public string PasswordHash { get; set; } = null!;

    [StringLength(30)]
    public string Role { get; set; } = null!;

    public bool IsActive { get; set; }

    public DateTime CreatedAt { get; set; }

    [InverseProperty("ReviewedByNavigation")]
    public virtual ICollection<Application> Applications { get; set; } = new List<Application>();

    [InverseProperty("VerifiedByNavigation")]
    public virtual ICollection<Document> Documents { get; set; } = new List<Document>();

    [InverseProperty("RecordedByNavigation")]
    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();
}
