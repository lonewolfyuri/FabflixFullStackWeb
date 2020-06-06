package main.java;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
    private static final long serialVersionUID = 2L;
    Document dom;
    String filepath = null;
    InputStream filestream = null;
    Map<String, Movie> movies;
    Map<String, Star> stars;
    Map<String, Cast> casts;
    List<String> messages;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    public XMLParser(InputStream inStream) throws IOException {
        movies = new HashMap<>();
        stars = new HashMap<>();
        casts = new HashMap<>();
        messages = new ArrayList<>();
        filestream = inStream;
        filepath = null;

        parseFile();
        parseDocument();
        //insertInDB();
    }

    public XMLParser(String fp) throws IOException {
        movies = new HashMap<>();
        stars = new HashMap<>();
        casts = new HashMap<>();
        messages = new ArrayList<>();
        filepath = fp;
        filestream = null;

        parseFile();
        parseDocument();
        //insertInDB();
    }

    public XMLParser(String fp, Map<String, Movie> mvs, Map<String, Star> strs, Map<String, Cast> csts) throws IOException {
        movies = mvs;
        stars = strs;
        casts = csts;
        messages = new ArrayList<>();
        filepath = fp;
        filestream = null;

        parseFile();
        parseDocument();
        //insertInDB();
    }

    private void parseFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            if (filepath != null) dom = db.parse(filepath);
            else dom = db.parse(filestream);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() throws IOException {
        Element docEle = dom.getDocumentElement();

        if (filepath.contains("mains")) {
            NodeList nldf = docEle.getElementsByTagName("directorfilms");
            if (nldf != null && nldf.getLength() > 0) {
                for (int ndxDF = 0; ndxDF < nldf.getLength(); ndxDF++) {
                    Element el = (Element) nldf.item(ndxDF);
                    try {
                        NodeList curDir = el.getElementsByTagName("director");

                        String director = getTextValue((Element) curDir.item(0), "dirname");

                        NodeList filmsNL = el.getElementsByTagName("films");
                        if (filmsNL != null && filmsNL.getLength() > 0) {
                            Element filmsEl = (Element) filmsNL.item(0);
                            NodeList nlf = filmsEl.getElementsByTagName("film");
                            if (nlf != null && nlf.getLength() > 0) {
                                for (int ndxF = 0; ndxF < nlf.getLength(); ndxF++) {
                                    Element curFilm = (Element) nlf.item(ndxF);
                                    try {
                                        String id = getTextValue(curFilm, "fid");
                                        if (!movies.containsKey(id)) {
                                            String title = getTextValue(curFilm, "t");
                                            String year = getTextValue(curFilm, "year");

                                            Map<String, Integer> genres = new HashMap<>();

                                            NodeList nlgs = curFilm.getElementsByTagName("cats");
                                            if (nlgs != null && nlgs.getLength() > 0) {
                                                Element cats = (Element) nlgs.item(0);
                                                NodeList nlg = cats.getElementsByTagName("cat");
                                                if (nlg != null && nlg.getLength() > 0) {
                                                    for (int ndxG = 0; ndxG < nlg.getLength(); ndxG++) {
                                                        try {
                                                            String gname = ((Element) nlg.item(ndxG)).getFirstChild().getNodeValue().trim();
                                                            int gid = -1;
                                                            for (String movie : movies.keySet()) {
                                                                Movie curMov = movies.get(movie);
                                                                if (curMov.genres.containsKey(gname)) gid = curMov.genres.get(gname);
                                                            }
                                                            genres.put(((Element) nlg.item(ndxG)).getFirstChild().getNodeValue().trim(), gid);
                                                        } catch (Exception e) {
                                                            messages.add(cats.toString());
                                                        }
                                                    }
                                                }
                                            }

                                            try {
                                                movies.put(id, new Movie(id, title, Integer.parseInt(year), director, 9.99, genres));
                                            } catch (Exception e) {
                                                messages.add(curFilm.toString());
                                            }
                                        }
                                    } catch (Exception e) {
                                        messages.add(curFilm.toString());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        messages.add(el.toString());
                    }
                }
            }
        } else if (filepath.contains("casts")) {
            NodeList nldf = docEle.getElementsByTagName("dirfilms");
            if (nldf != null && nldf.getLength() > 0) {
                for (int ndxDF = 0; ndxDF < nldf.getLength(); ndxDF++) {
                    Element df = (Element) nldf.item(ndxDF);
                    try {
                        NodeList nlf = df.getElementsByTagName("filmc");
                        if (nlf != null && nlf.getLength() > 0) {
                            for (int ndxF = 0; ndxF < nlf.getLength(); ndxF++) {
                                String movie_id = null;
                                Element film = (Element) nlf.item(ndxF);
                                try {
                                    NodeList nls = film.getElementsByTagName("m");
                                    if (nls != null && nls.getLength() > 0) {
                                        Map<String, String> star_names = new HashMap<>();
                                        for (int ndxM = 0; ndxM < nls.getLength(); ndxM++) {
                                            Element star = (Element) nls.item(ndxM);
                                            String name = getTextValue(star, "a");
                                            try {
                                                if (ndxM == 0) movie_id = getTextValue(star, "f");
                                                if (!star_names.containsKey(name)) star_names.put(name, null);
                                            } catch (Exception e) {
                                                messages.add(star.toString());
                                            }
                                        }
                                        if (!casts.containsKey(movie_id)) casts.put(movie_id, new Cast(movie_id, star_names));
                                    }
                                } catch (Exception e) {
                                    messages.add(film.toString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        messages.add(df.toString());
                    }
                }
            }
        } else if (filepath.contains("actors")) {
            NodeList nla = docEle.getElementsByTagName("actor");
            if (nla != null && nla.getLength() > 0) {
                for (int ndxA = 0; ndxA < nla.getLength(); ndxA++) {
                    Element actor = (Element) nla.item(ndxA);
                    String name = getTextValue(actor, "stagename");
                    try {
                        if (!stars.containsKey(name)) stars.put(name, new Star(getTextValue(actor, "stagename"), Integer.parseInt(getTextValue(actor, "dob"))));
                    } catch (Exception e) {
                        try {
                            if (!stars.containsKey(name)) stars.put(name, new Star(getTextValue(actor, "stagename"), -1));
                        } catch (Exception e2) {
                            messages.add(actor.toString());
                        }
                    }
                }
            }
        }

        FileWriter oFile = new FileWriter("messages.txt");
        oFile.append("\n\nBegin Logging Messages for: " + filepath + "\n\n");
        for (String msg : messages) oFile.append(msg + "\n");
        oFile.close();
    }

    /*
    private String generateStatements() {
        try {
            Connection dbcon = dataSource.getConnection();

            for (Movie curMov : movies) {
                PreparedStatement statement = dbcon.prepareStatement("CALL add_movie_basic(?, ?, ?, ?, ?);");

                try {
                    statement.setString(1, curMov.id);
                    statement.setString(2, curMov.title);
                    statement.setInt(3, curMov.year);
                    statement.setString(4, curMov.director);
                    statement.setDouble(1, curMov.price);

                    statement.executeUpdate();
                } catch (Exception e) {

                }

                for (String genre : curMov.genres) {
                    try {
                        statement = dbcon.prepareStatement("CALL add_movie_genre(?, ?)");

                        statement.setString(1, curMov.id);
                        statement.setString(2, genre);

                        statement.executeUpdate();
                    } catch (Exception e) {

                    }
                }
            }
        } catch (Exception e) {

        }

        try {
            Connection dbcon = dataSource.getConnection();

            for (Star curStar : stars) {
                PreparedStatement statement = dbcon.prepareStatement("select max(id) as id from stars;");

                try {
                    ResultSet rs = statement.executeQuery();

                    String id = "";
                    if (rs.next()) {
                        id = rs.getString("id");
                    }

                    int num = Integer.parseInt(id.substring(2));
                    num++;

                    id = id.substring(0, 2) + num;

                    if (curStar.year > 0) {
                        statement = dbcon.prepareStatement("CALL add_star(?, ?, ?)");
                        statement.setString(1, id);
                        statement.setString(2, curStar.name);
                        statement.setInt(3, curStar.year);
                    } else {
                        statement = dbcon.prepareStatement("CALL add_star(?, ?)");
                        statement.setString(1, id);
                        statement.setString(2, curStar.name);
                    }

                    statement.executeUpdate();
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        }

        try {
            Connection dbcon = dataSource.getConnection();

            for (Cast curCast : casts) {
                for (String star : curCast.stars) {
                    try {
                        PreparedStatement statement = dbcon.prepareStatement("select max(id) as id from stars;");
                        ResultSet rs = statement.executeQuery();

                        String id = "";
                        if (rs.next()) {
                            id = rs.getString("id");
                        }

                        int num = Integer.parseInt(id.substring(2));
                        num++;

                        id = id.substring(0, 2) + num;

                        statement = dbcon.prepareStatement("CALL add_movie_star(?, ?, ?)");

                        statement.setString(1, curCast.movie_id);
                        statement.setString(2, star);
                        statement.setString(3, id);

                        statement.executeUpdate();
                    } catch (Exception e) {

                    }
                }
            }
        } catch (Exception e) {

        }
    }

     */

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            try {
                textVal = el.getFirstChild().getNodeValue();
            } catch (Exception e) {
                return null;
            }
        }

        return textVal;
    }

    public void printMsgs() {
        for (String msg : messages) System.out.println("Entry Format Invalid: " + msg);
    }

    public List<String> getResult() {
        return messages;
    }

    public static void main(String[] args) throws IOException {
        XMLParser curPrsr = new XMLParser("stanford-movies/mains243.xml");
        curPrsr = new XMLParser("stanford-movies/actors63.xml");
        curPrsr = new XMLParser("stanford-movies/casts124.xml");

        System.out.println("done");
    }
}
