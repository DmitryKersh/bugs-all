package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.util.Evaluator;
import com.github.dmitrykersh.bugs.api.util.NdList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents initial state of the board (initial state of all tiles)
 * It maps tile IDs to states and players
 */
public class Layout {
    class TileTemplate {
        public TileTemplate(int id, int ownerNumber, TileState state) {
            this.id = id;
            this.ownerNumber = ownerNumber;
            this.state = state;
        }

        private int id;
        private int ownerNumber;
        private TileState state;
    }

    public Layout() {
        params = new HashMap<>();
        tiles = new HashMap<>();
    }

    private String description;
    private Map<String, Integer> params;
    private Map<Integer, TileTemplate> tiles;

    public void LoadLayout(String filename) {
        // TODO: XSD validation of input file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filename));
            doc.getDocumentElement().normalize();

            // [0] - description
            // [1] - parameters
            // [2] - tile array
            NdList nodes = new NdList(doc.getDocumentElement().getChildNodes());
            description = nodes.item(0).getTextContent();

            NdList paramList = new NdList(nodes.item(1).getChildNodes());
            for (int i = 0; i < paramList.getLength(); i++)
                processNodeAsParam(paramList.item(i));

            NdList tileList = new NdList(nodes.item(2).getChildNodes());
            for (int i = 0; i < tileList.getLength(); i++)
                processNodeAsTile(tileList.item(i));

            System.out.println();
        } catch (IllegalArgumentException | ParserConfigurationException | SAXException |IOException e) {
            e.printStackTrace();
        }
    }

    private void processNodeAsParam(Node node) {
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

    private void processNodeAsTile(Node node) {
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
            Integer id = Evaluator.evaluateSimpleEquationAsInt(idStr);
            tiles.put(id, new TileTemplate(id,
                    Integer.parseInt(children.item(1).getTextContent()),
                    TileState.valueOf(children.item(2).getTextContent())));
        }
    }
}


