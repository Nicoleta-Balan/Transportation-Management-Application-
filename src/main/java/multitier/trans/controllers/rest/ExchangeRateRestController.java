package multitier.trans.controllers.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@CrossOrigin(origins = "*")
public class ExchangeRateRestController {

    private static final String BNR_URL = "https://www.bnr.ro/nbrfxrates.xml";

    // Cache the rates for 1 hour to avoid hitting BNR too frequently
    private Map<String, Double> cachedRates = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 3600000; // 1 hour

    @GetMapping
    public ResponseEntity<?> getExchangeRates() {
        try {
            // Check cache
            if (cachedRates != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS) {
                return ResponseEntity.ok(cachedRates);
            }

            // Fetch from BNR
            URL url = new URL(BNR_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return ResponseEntity.ok(getFallbackRates());
            }

            InputStream inputStream = connection.getInputStream();

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            Map<String, Double> rates = new HashMap<>();

            // Get all Rate elements
            NodeList rateNodes = document.getElementsByTagName("Rate");

            for (int i = 0; i < rateNodes.getLength(); i++) {
                Element rateElement = (Element) rateNodes.item(i);
                String currency = rateElement.getAttribute("currency");
                String multiplierStr = rateElement.getAttribute("multiplier");
                String valueStr = rateElement.getTextContent().trim();

                if (currency.equals("EUR") || currency.equals("USD") || currency.equals("GBP")) {
                    double value = Double.parseDouble(valueStr);
                    int multiplier = multiplierStr.isEmpty() ? 1 : Integer.parseInt(multiplierStr);
                    rates.put(currency, value / multiplier);
                }
            }

            inputStream.close();
            connection.disconnect();

            // Ensure we have all required currencies
            if (!rates.containsKey("EUR")) rates.put("EUR", 4.97);
            if (!rates.containsKey("USD")) rates.put("USD", 4.56);
            if (!rates.containsKey("GBP")) rates.put("GBP", 5.78);

            // Update cache
            cachedRates = rates;
            cacheTimestamp = System.currentTimeMillis();

            return ResponseEntity.ok(rates);

        } catch (Exception e) {
            System.err.println("Error fetching BNR rates: " + e.getMessage());
            return ResponseEntity.ok(getFallbackRates());
        }
    }

    private Map<String, Double> getFallbackRates() {
        Map<String, Double> fallback = new HashMap<>();
        fallback.put("EUR", 4.97);
        fallback.put("USD", 4.56);
        fallback.put("GBP", 5.78);
        return fallback;
    }
}