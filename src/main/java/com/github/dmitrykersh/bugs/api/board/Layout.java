package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.util.Evaluator;
import com.github.dmitrykersh.bugs.api.util.NdList;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents initial state of the board (initial state of all tiles)
 * It maps tile IDs to states and players
 */
public class Layout {
    /**
     * This class represents a tile template, from which a Tile will be created by Board. It stores initial owner's
     * number in player list and initial TileState.
     */
    class TileTemplate {
        private final int ownerNumber;
        private final TileState state;

        public TileTemplate(final int ownerNumber, final @NotNull TileState state) {
            this.ownerNumber = ownerNumber;
            this.state = state;
        }

        public int getOwnerNumber() {
            return ownerNumber;
        }

        public TileState getState() {
            return state;
        }
    }

    class PlayerTemplate {
        private final int maxTurns;

        public int getMaxTurns() {
            return maxTurns;
        }

        PlayerTemplate(int maxTurns) {
            this.maxTurns = maxTurns;
        }
    }

    private String description;
    private Map<Integer, PlayerTemplate> players;
    private Map<String, Integer> params;
    private Map<Integer, TileTemplate> tiles;

    public Layout() {
        params = new HashMap<>();
        tiles = new HashMap<>();
        players = new HashMap<>();
    }

    public Layout(final @NotNull Map<String, Integer> params) {
        this.params = params;
        tiles = new HashMap<>();
        players = new HashMap<>();
    }

    public TileTemplate getTileTemplate(int id) {
        return tiles.get(id);
    }
    public PlayerTemplate getPlayerTemplate(int id) { return players.get(id); }

    /**
     * This method loads data from XML document to Layout.
     * Layout.tiles stores TileTemplate-s, loaded from XML, after counting their IDs using parameters' values
     * specified in Layout.params (or default values specified in XML).
     *
     * @param filename path to file
     */
    public void LoadLayout(final @NotNull String filename) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(filename));
            doc.getDocumentElement().normalize();

            Schema schema = loadSchema("src\\main\\resources\\layout.xsd");
            validateXml(schema, doc);

            // [0] - description
            // [1] - parameters
            // [2] - player array
            // [3] - tile array
            NdList nodes = new NdList(doc.getDocumentElement().getChildNodes());
            description = nodes.item(0).getTextContent();

            NdList paramList = new NdList(nodes.item(1).getChildNodes());
            for (int i = 0; i < paramList.getLength(); i++)
                processNodeAsParam(paramList.item(i));

            NdList playerList = new NdList(nodes.item(2).getChildNodes());
            for (int i = 0; i < playerList.getLength(); i++)
                processNodeAsPlayer(playerList.item(i));

            NdList tileList = new NdList(nodes.item(3).getChildNodes());
            for (int i = 0; i < tileList.getLength(); i++)
                processNodeAsTile(tileList.item(i));

            System.out.println();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after document validation by schema
     * Creates a layout parameter from Document Node assuming it's valid by schema.
     * If parameter with the same name is already present in Layout.params,
     * the value is NOT overwritten
     * @param node input Document Node
     */
    private void processNodeAsParam(final @NotNull Node node) {
        if (node.getNodeName().equals("Param")) {
            // [0] - name
            // [1] - default value
            // [2] - description
            NdList children = new NdList(node.getChildNodes());
            if (!params.containsKey(children.item(0).getTextContent())) {
                // if parameter is not set then it's set by default as stated in layout file
                params.put(children.item(0).getTextContent(), Integer.parseInt(children.item(1).getTextContent()));
            }
        }
    }

    /**
     * Called after document validation by schema
     * Creates PlayerTemplate from Document.
     * Default player is stored at key 0 in `players` map
     * @param node input Document Node
     */
    private void processNodeAsPlayer(final @NotNull Node node) {
        NdList children = new NdList(node.getChildNodes());
        if (node.getNodeName().equals("Default")) {
            players.put(0, new PlayerTemplate(Integer.parseInt(children.item(0).getTextContent())));
        } else {
            players.put(Integer.parseInt(children.item(0).getTextContent()),
                    new PlayerTemplate(Integer.parseInt(children.item(1).getTextContent())));
        }
    }

    /**
     * Called after document validation by schema
     * Creates a layout TileTemplate from Document Node assuming it's valid by schema.
     * Evaluates tile ID with parameters' values. Takes owner number
     * and initial state as is.
     * @param node input Document Node
     */
    private void processNodeAsTile(final @NotNull Node node) {
        if (node.getNodeName().equals("Tile")) {
            // [0] - id
            // [1] - owner number
            // [2] - initial state
            NdList children = new NdList(node.getChildNodes());
            String idStr = children.item(0).getTextContent();
            // apply parameters
            for (Map.Entry<String, Integer> param : params.entrySet()) {
                idStr = idStr.replace(param.getKey(), param.getValue().toString());
            }
            Integer id = Evaluator.evaluateComplexEquationAsInt(idStr);
            tiles.put(id, new TileTemplate(Integer.parseInt(children.item(1).getTextContent()),
                    TileState.valueOf(children.item(2).getTextContent())));
        }
    }

    private static Schema loadSchema(final @NotNull String schemaFileName) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return factory.newSchema(new File(schemaFileName));
    }

    private static void validateXml(final @NotNull Schema schema, final @NotNull Document document) throws IOException, SAXException {
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }
}


