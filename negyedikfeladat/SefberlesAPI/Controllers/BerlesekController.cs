using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SefberlesAPI.Data;
using SefberlesAPI.Models;

namespace SefberlesAPI.Controllers
{
    [ApiController]
    [Route("api/berlesek")]
    public class BerlesekController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BerlesekController(AppDbContext context)
        {
            _context = context;
        }

        // GET /api/berlesek
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Berles>>> GetAll()
        {
            return await _context.Berlesek.ToListAsync();
        }

        // GET /api/berlesek/{id}
        [HttpGet("{id}")]
        public async Task<ActionResult<Berles>> GetById(int id)
        {
            var berles = await _context.Berlesek.FindAsync(id);
            if (berles == null)
                return NotFound();
            return berles;
        }

        // POST /api/berlesek
        [HttpPost]
        public async Task<ActionResult<Berles>> Create(Berles berles)
        {
            var holnap = DateTime.Today.AddDays(1);

            // 1. A bérlés kezdőnapja nem lehet korábbi, mint a holnapi nap
            if (berles.StartDate.Date < holnap)
                return BadRequest("A bérlés kezdőnapja nem lehet korábbi, mint a holnapi nap.");

            var idotartam = (berles.EndDate.Date - berles.StartDate.Date).TotalDays;

            // 2. A bérlés időtartama legalább 3 nap legyen
            if (idotartam < 3)
                return BadRequest("A bérlés időtartama legalább 3 nap kell legyen.");

            // 3. A bérlés időtartama legfeljebb 14 nap lehet
            if (idotartam > 14)
                return BadRequest("A bérlés időtartama legfeljebb 14 nap lehet.");

            // 4. Átfedés ellenőrzése ugyanarra a séfre
            bool atfedes = await _context.Berlesek.AnyAsync(b =>
                b.ChefId == berles.ChefId &&
                b.StartDate.Date < berles.EndDate.Date &&
                b.EndDate.Date > berles.StartDate.Date);

            if (atfedes)
                return BadRequest("Ez a séf a megadott időszakban már le van foglalva.");

            _context.Berlesek.Add(berles);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetById), new { id = berles.Id }, berles);
        }

        // DELETE /api/berlesek/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var berles = await _context.Berlesek.FindAsync(id);
            if (berles == null)
                return NotFound();

            _context.Berlesek.Remove(berles);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}
