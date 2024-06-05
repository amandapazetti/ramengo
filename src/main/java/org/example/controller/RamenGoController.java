package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.dto.BrothResponse;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.dto.ProteinResponse;
import org.example.model.Broth;

import org.example.model.Protein;
import org.example.service.BrothService;
import org.example.service.OrderService;
import org.example.service.ProteinService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Api(tags = "default", description = "")
public class RamenGoController {

    @Autowired
    private BrothService brothService;

    @Autowired
    private ProteinService proteinService;

    @Autowired
    private OrderService orderService;

    private final String API_KEY = "passei";

    @ApiOperation(value = "List all available broths")
    @GetMapping("/broths")
    public ResponseEntity<?> listBroths(@RequestHeader(value = "x-api-key", required = false) String apiKey) {
        if (apiKey == null || !apiKey.equals(API_KEY)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "x-api-key header missing");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        List<Broth> broths = brothService.getAllBroths();
        List<BrothResponse> response = broths.stream().map(broth -> new BrothResponse(
                String.valueOf(broth.getId()),
                "https://tech.redventures.com.br/icons/salt/inactive.svg",
                "https://tech.redventures.com.br/icons/salt/active.svg",
                broth.getName(),
                broth.getDescription(),
                broth.getPrice()
        )).collect(Collectors.toList());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

    }

    @ApiOperation(value = "List all available proteins")
    @GetMapping("/proteins")
    public ResponseEntity<?> listProteins(@RequestHeader(value = "x-api-key", required = false) String apiKey) {
        if (apiKey == null || !apiKey.equals(API_KEY)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "x-api-key header missing");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        List<Protein> proteins = proteinService.getAllProteins();
        List<ProteinResponse> response = proteins.stream().map(protein -> new ProteinResponse(
                String.valueOf(protein.getId()),
                "https://tech.redventures.com.br/icons/pork/inactive.svg",
                "https://tech.redventures.com.br/icons/pork/active.svg",
                protein.getName(),
                protein.getDescription(),
                protein.getPrice()
        )).collect(Collectors.toList());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping
    @ApiOperation(value = "Place an order")
    public ResponseEntity<?> placeOrder(@RequestHeader(value = "x-api-key", required = false) String apiKey, @RequestBody OrderRequest orderRequest) {
        if (apiKey == null || !apiKey.equals(API_KEY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<String, String>() {{
                put("error", "x-api-key header missing");
            }});
        }

        if (orderRequest.getBrothId() == null || orderRequest.getProteinId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, String>() {{
                put("error", "both brothId and proteinId are required");
            }});
        }

        try {
            OrderResponse placedOrder = orderService.placeOrder(orderRequest);

            OrderResponse orderResponse = new OrderResponse();
            orderResponse.setId(String.valueOf(placedOrder.getId()));
            orderResponse.setDescription("Salt and Chasu Ramen");
            orderResponse.setImage("https://tech.redventures.com.br/icons/ramen/ramenChasu.png");

            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "could not place order");
            }});
        }
    }
}