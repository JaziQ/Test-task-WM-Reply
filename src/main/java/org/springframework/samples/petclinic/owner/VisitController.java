/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

	private final VisitRepository visitsRepo;

	private final PetRepository pets;

	private final VetRepository vets;

	public VisitController(VisitRepository visitsRepo, PetRepository pets, VetRepository vets) {
		this.visitsRepo = visitsRepo;
		this.pets = pets;
		this.vets = vets;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 *
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
		Visit visit = new Visit();
		Pet pet = this.pets.findById(petId);
		Collection<Vet> vets = this.vets.findAll();
		pet.setVisitsInternal(this.visitsRepo.findByPetId(petId));
		model.put("pet", pet);
		model.put("vets", vets);
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
	@GetMapping("/owners/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		} else {
			this.visitsRepo.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/owners/*/pets/{petId}/visits/{visitId}/edit")
	public String initEditVisitForm(@PathVariable("petId") int petId, @PathVariable("visitId") Integer visitId, Map<String, Object> model) {
		return "pets/updateVisitForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String processEditVisitForm(@PathVariable("visitId") int visitId, @Valid Visit oldVisit, BindingResult result) {
		if (result.hasErrors()) {
			return "pets/updateVisitForm";
		} else {
			Visit newVisit = visitsRepo.findById(visitId);
			newVisit.setDate(oldVisit.getDate());
			newVisit.setVetId(oldVisit.getVetId());
			newVisit.setDescription(oldVisit.getDescription());
			this.visitsRepo.save(newVisit);
			return "redirect:/owners/{ownerId}";
		}
	}
}
