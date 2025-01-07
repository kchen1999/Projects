import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    public Rasterer() {
      //TODO
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */

    // Longitude is x coordinate, latitude is y coordinate
    // d0 - 1
    // d1 - 2
    // d2 - 4
    // d3 - 8
    // d4 - 16
    // d5 - 32
    // d6 - 64
    // d7 - 128
    /*
        Calculates longitudinal distance per pixel
     */
    private double calculateLonDPP(double lrlon, double ullon, double w) {
        return (lrlon - ullon) / w;
    }

    /*
        The images that you return as a String[][] when rastering must be those that:
        Include any region of the query box.
        Have the greatest LonDPP that is less than or equal to the LonDPP of the query box
        (as zoomed out as possible). If the requested LonDPP is less than what is available in the data
        files, you should use the lowest LonDPP available instead (i.e. depth 7 images).
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        double queryBoxLonDPP = calculateLonDPP(params.get("lrlon"), params.get("ullon"), params.get("w"));
        return results;
    }
}
