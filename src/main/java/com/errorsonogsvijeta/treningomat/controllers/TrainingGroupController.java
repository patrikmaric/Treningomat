package com.errorsonogsvijeta.treningomat.controllers;

import com.errorsonogsvijeta.treningomat.model.training.TrainingGroup;
import com.errorsonogsvijeta.treningomat.model.users.Attendant;
import com.errorsonogsvijeta.treningomat.model.users.Trainer;
import com.errorsonogsvijeta.treningomat.services.AttendantService;
import com.errorsonogsvijeta.treningomat.services.TrainerService;
import com.errorsonogsvijeta.treningomat.services.TrainingGroupService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik
 */

@Controller
public class TrainingGroupController {

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private TrainingGroupService trainingGroupService;
    @Autowired
    private AttendantService attendantService;

    @RequestMapping(value = "/trainer/addTrainingGroup", method = RequestMethod.GET)
    public ModelAndView addGroup() {
        return getAddGroupModelAndView("");
    }


    //TODO: dodaj u JavaScriptu provjeru jeli korisnik zapravo popunio sva polja
    @RequestMapping(value = "/trainer/addTrainingGroup", method = RequestMethod.POST)
    public ModelAndView createNewGroup(@Valid TrainingGroup trainingGroup, BindingResult groupResult, HttpServletRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Trainer trainer = trainerService.findTrainerByUsername(user.getUsername());

        trainingGroup.setTrainer(trainer);

        String message;
        try {
            trainingGroupService.saveTrainingGroup(trainingGroup);
            message = "Training group successfully added.";
        } catch (Exception e) {
            message = "Failed to add training group!";
        }

        return getAddGroupModelAndView(message);
    }

    private ModelAndView getAddGroupModelAndView(String message) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Trainer trainer = trainerService.findTrainerByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("trainingGroup", new TrainingGroup());
        modelAndView.addObject("trainersSports", trainer.getSports());
        modelAndView.setViewName("trainer/add_training_group");
        if (!message.equals("")) {
            modelAndView.addObject("message", message);
        }

        return modelAndView;
    }

    @RequestMapping(value = "/trainer/groups", method = RequestMethod.GET)
    public ModelAndView viewTrainersGroups() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Trainer trainer = trainerService.findTrainerByUsername(user.getUsername());

        List<TrainingGroup> trainingGroups = trainingGroupService.getTrainersTrainingGroups(trainer);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("allTrainingGroups", trainingGroups);
        modelAndView.setViewName("/trainer/trainer_groups");

        return modelAndView;
    }

    @RequestMapping(value = "/trainer/groups/edit/{id}", method = RequestMethod.GET)
    public ModelAndView editGroup(@PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Trainer trainer = trainerService.findTrainerByUsername(user.getUsername());

        TrainingGroup trainingGroup = trainingGroupService.getTrainingGroup(id);

        modelAndView.addObject("trainingGroup", trainingGroup);
        modelAndView.setViewName("trainer/edit_group");
        modelAndView.addObject("trainersSports", trainer.getSports());

        return modelAndView;
    }

    //tu bi mozda trebao dodati i izbacivanje korisnika iz grupe, da u prikazu
    @RequestMapping(value = "/trainer/groups/edit/{id}", method = RequestMethod.POST)
    public ModelAndView updateGroup(@PathVariable("id") Integer id, HttpServletRequest request, TrainingGroup trainingGroup, RedirectAttributes redirectAttributes) {
        boolean changed = false;

        TrainingGroup oldTrainingGroup = trainingGroupService.getTrainingGroup(id);

        if (!oldTrainingGroup.getName().equals(trainingGroup.getName())) {
            oldTrainingGroup.setName(trainingGroup.getName());
            changed = true;
        }
        if (!oldTrainingGroup.getPlace().equals(trainingGroup.getPlace())) {
            oldTrainingGroup.setPlace(trainingGroup.getPlace());
            changed = true;
        }
        if (!oldTrainingGroup.getSport().equals(trainingGroup.getSport())) {
            oldTrainingGroup.setSport(trainingGroup.getSport());
            changed = true;
        }

        //nije moguce smanjiti kapacitet vise od trenutnog broja clanova
        List<Attendant> attendants = attendantService.getAllAttendantsOfAGroup(trainingGroup);
        if (trainingGroup.getCapacity() != null && trainingGroup.getCapacity() >= attendants.size()
                && !oldTrainingGroup.getCapacity().equals(trainingGroup.getCapacity())) {
            oldTrainingGroup.setCapacity(trainingGroup.getCapacity());
            changed = true;
        }

        if (changed) {
            trainingGroupService.saveTrainingGroup(oldTrainingGroup);
            redirectAttributes.addFlashAttribute("message", "Grupa izmjenjena.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Krivi unos!");
        }

        return new ModelAndView("redirect:" + ("/trainer/groups/edit/" + id));
    }

    @RequestMapping(value = "/trainer/groups/delete/{id}", method = RequestMethod.POST)
    public ModelAndView deleteGroup(@PathVariable("id") String id) {
        if (id != null) {
            trainingGroupService.deleteTrainingGroup(Integer.parseInt(id));
        }
        //TODO: dodaj jos provjeru jeli trener siguran zeli li obrisati grupu(JS)
        return new ModelAndView("redirect:" + "/trainer/groups");
    }

    @RequestMapping(value = "/trainer/group/{id}/attendants", method = RequestMethod.GET)
    public ModelAndView getListOfAttendants(@PathVariable("id") Integer id) {
        List<Attendant> attendants = new ArrayList<>();
        if (id != null) {
            attendants = attendantService.getAllAttendantsOfAGroup(trainingGroupService.getTrainingGroup(id));
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("attendants", attendants);
        modelAndView.addObject("groupId", id);
        modelAndView.setViewName("/trainer/group_attendants");

        return modelAndView;
    }

    //TODO: provjeri postoji li bolji nacin za ovo, i u JQuerry-u dodat provjeru jeli je trener siguran
    @RequestMapping(value = "/trainer/group/{groupId}/attendant/{attendantId}", method = RequestMethod.POST)
    public ModelAndView removeAttendantFromGroup(@PathVariable("groupId") Integer groupId, @PathVariable("attendantId") Integer attendantId) {
        TrainingGroup trainingGroup = trainingGroupService.getTrainingGroup(groupId);
        Attendant attendant = attendantService.getAttendantById(attendantId);

        for (Attendant a : trainingGroup.getAttendants()) {
            if (a.equals(attendant)) {
                trainingGroup.getAttendants().remove(a);
                break;
            }
        }

        trainingGroupService.saveTrainingGroup(trainingGroup);

        return new ModelAndView("redirect:" + "/trainer/group/" + groupId + "/attendants");
    }


}
