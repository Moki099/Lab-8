package kz.iitu.registration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.iitu.registration.model.EventInfo;
import kz.iitu.registration.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void helloReturnsWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Welcome to the Event Registration API")));
    }

    @Test
    void infoReturnsValuesFromApplicationConfiguration() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName", is("event-registration-api")))
                .andExpect(jsonPath("$.course", is("Web Technologies")))
                .andExpect(jsonPath("$.semester", is("3")));
    }

    @Test
    void getAllEventsReturnsInitialEvents() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Web Design Workshop")))
                .andExpect(jsonPath("$[1].title", is("JavaScript Quiz")))
                .andExpect(jsonPath("$[2].title", is("IT Career Talk")));
    }

    @Test
    void getEventByIdReturnsEvent() throws Exception {
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Web Design Workshop")));
    }

    @Test
    void missingEventReturns404() throws Exception {
        mockMvc.perform(get("/api/events/999999")).andExpect(status().isNotFound());
    }

    @Test
    void malformedIdReturns400() throws Exception {
        mockMvc.perform(get("/api/events/not-a-number")).andExpect(status().isBadRequest());
    }

    @Test
    void titleSearchIgnoresCase() throws Exception {
        mockMvc.perform(get("/api/events").param("title", "qUiZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("JavaScript Quiz")));
    }

    @Test
    void titleSearchWithNoMatchReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/events").param("title", "no such event"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void postCreatesEventWithServerAssignedIdAndReturns201() throws Exception {
        int initialSize = eventService.getAllEvents().size();
        MvcResult result = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"Temporary test event\",\"location\":\"Test room\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Temporary test event")))
                .andExpect(jsonPath("$.location", is("Test room")))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        long id = response.get("id").asLong();
        try {
            assertTrue(id >= 4);
            assertEquals(initialSize + 1, eventService.getAllEvents().size());
            mockMvc.perform(get("/api/events/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is((int) id)));
        } finally {
            eventService.deleteEvent(id);
        }
    }

    @Test
    void postRejectsBlankOrMissingFieldsAndNullBodyWithoutChangingList() throws Exception {
        int initialSize = eventService.getAllEvents().size();
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"location\":\"Room\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid\",\"location\":\"  \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
        assertEquals(initialSize, eventService.getAllEvents().size());
    }

    @Test
    void putUpdatesBothFieldsAndPreservesId() throws Exception {
        EventInfo original = createEvent();
        try {
            mockMvc.perform(put("/api/events/{id}", original.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":999,\"title\":\"Updated event\",\"location\":\"Room 42\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(original.id().intValue())))
                    .andExpect(jsonPath("$.title", is("Updated event")))
                    .andExpect(jsonPath("$.location", is("Room 42")));
        } finally {
            eventService.deleteEvent(original.id());
        }
    }

    @Test
    void putMissingIdReturns404AndInvalidInputDoesNotChangeEvent() throws Exception {
        mockMvc.perform(put("/api/events/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid\",\"location\":\"Room\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null,\"location\":\"Room\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Web Design Workshop")))
                .andExpect(jsonPath("$.location", is("Room 301")));
        mockMvc.perform(put("/api/events/1").contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturns204AndMissingIdReturns404() throws Exception {
        EventInfo created = createEvent();
        mockMvc.perform(delete("/api/events/{id}", created.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(get("/api/events/{id}", created.id())).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/events/{id}", created.id())).andExpect(status().isNotFound());
    }

    @Test
    void deletedIdIsNotReused() {
        EventInfo first = createEvent();
        eventService.deleteEvent(first.id());
        EventInfo second = createEvent();
        try {
            assertTrue(second.id() > first.id());
        } finally {
            eventService.deleteEvent(second.id());
        }
    }

    private EventInfo createEvent() {
        return eventService.addEvent("Temporary test event", "Test room");
    }
}