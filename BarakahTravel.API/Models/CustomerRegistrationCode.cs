using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

[Index("RegistrationCode", Name = "UQ__Customer__A94D9FED43CB33E6", IsUnique = true)]
public partial class CustomerRegistrationCode
{
    [Key]
    [Column("RegistrationCodeID")]
    public int RegistrationCodeId { get; set; }

    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [StringLength(50)]
    public string RegistrationCode { get; set; } = null!;

    public DateTime CreatedAt { get; set; }

    public DateTime? ExpiresAt { get; set; }

    public bool IsUsed { get; set; }

    public bool IsActive { get; set; }

    [ForeignKey("CustomerId")]
    [InverseProperty("CustomerRegistrationCodes")]
    public virtual Customer Customer { get; set; } = null!;
}
