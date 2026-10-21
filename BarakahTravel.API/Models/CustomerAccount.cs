using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

[Index("CustomerId", Name = "UQ__Customer__A4AE64B962517449", IsUnique = true)]
[Index("Email", Name = "UQ__Customer__A9D10534F4BEF480", IsUnique = true)]
public partial class CustomerAccount
{
    [Key]
    [Column("CustomerAccountID")]
    public int CustomerAccountId { get; set; }

    [Column("CustomerID")]
    public int CustomerId { get; set; }

    [StringLength(100)]
    public string Email { get; set; } = null!;

    [StringLength(500)]
    public string PasswordHash { get; set; } = null!;

    public bool IsActive { get; set; }

    public DateTime CreatedAt { get; set; }

    [ForeignKey("CustomerId")]
    [InverseProperty("CustomerAccount")]
    public virtual Customer Customer { get; set; } = null!;
}
