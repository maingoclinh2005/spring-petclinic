package org.springframework.samples.petclinic.owner;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PetExportController {

	private final OwnerRepository owners;

	public PetExportController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/pets/export")
	public void exportPets(HttpServletResponse response) throws IOException {
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=pet-data.csv");

		PrintWriter writer = response.getWriter();
		writer.write('\ufeff');
		writer.println("Pet ID,Pet Name,Birth Date,Type,Owner ID,Owner Name,Telephone,City");

		for (Owner owner : this.owners.findAll()) {
			for (Pet pet : owner.getPets()) {
				writer.println(csv(pet.getId()) + "," + csv(pet.getName()) + "," + csv(pet.getBirthDate()) + ","
						+ csv(pet.getType() == null ? "" : pet.getType().getName()) + "," + csv(owner.getId()) + ","
						+ csv(owner.getFirstName() + " " + owner.getLastName()) + "," + csv(owner.getTelephone()) + ","
						+ csv(owner.getCity()));
			}
		}
		writer.flush();
	}

	private String csv(Object value) {
		String text = value == null ? "" : value.toString();
		return "\"" + text.replace("\"", "\"\"") + "\"";
	}

}
