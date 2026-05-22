using Microsoft.EntityFrameworkCore;
using SefberlesAPI.Models;

namespace SefberlesAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Berles> Berlesek { get; set; }
    }
}
