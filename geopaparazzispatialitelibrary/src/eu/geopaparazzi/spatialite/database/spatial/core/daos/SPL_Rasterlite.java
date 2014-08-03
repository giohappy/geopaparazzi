/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.geopaparazzi.spatialite.database.spatial.core.daos;

import eu.geopaparazzi.library.database.GPLog;
import eu.geopaparazzi.spatialite.database.spatial.core.tables.AbstractSpatialTable;
import eu.geopaparazzi.spatialite.database.spatial.util.SpatialiteUtilities;
import jsqlite.Database;
import jsqlite.Stmt;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SPL_Rasterlite related doa.
 *
 * @author Mark Johnson
 */
public class SPL_Rasterlite {

    /**
     * Zoom-Level resolutions.
     * <p/>
     * Note: Resolutions are base on Gdal-Georeferenced Images of Tiles
     * - for Brandenburger Tor, Berlin, Germany
     * -- tile_osm : 20/563253/343903
     * -- tile_tms : 20/563253/704672
     * -- SRID=4326;POLYGON((13.377571106 52.516429788,13.377914429 52.516429788,13.377914429 52.516220864,13.377571106 52.516220864,13.377571106 52.516429788))
     * <p/>
     * gdal_translate -gcp 0 256 13.377571106 52.516220864 -gcp 256 0 13.377914429 52.516429788  -gcp 0 0 13.377571106 52.516429788  -gcp 256 256  13.377914429 52.516220864 20_563253_343903_osm.png 20_563253_343903_osm.gcp.tif
     * gdalwarp -s_srs EPSG:4326 -t_srs EPSG:3395  20_563253_343903_osm.gcp.tif 20_563253_343903_osm.3395.tif 
     * gdalinfo 20_563253_343903_osm.3395.tif  | grep Pixel
     * <p/>
     * - 20_563253_343903_osm.3395.tif: Pixel Size = (0.149105396388009,-0.149105396388009)*256= 38.170 meters
     * -- 'WGS 84 / World Mercator'
     * - meters : per tile á 256 pixels
     * <p/>
     * - list is NOT sorted
     */
    public static LinkedHashMap<String, Double> mercator_resolutions = new LinkedHashMap<String, Double>();

    static {
      mercator_resolutions.put("Zoom  0 - 1:500 Million - 40032406.294 meters", 156376.587086746090790);
      mercator_resolutions.put("Zoom  1 - 1:250 Million - 20016203.147 meters", 78188.293543373045395);
      mercator_resolutions.put("Zoom  2 - 1:150 Million - 9999156.402 meters", 39059.204695334061398);
      mercator_resolutions.put("Zoom  3 - 1:70 Million - 4995382.840 meters", 19513.214221145241027);
      mercator_resolutions.put("Zoom  4 - 1:35 Million - 2501025.826 meters", 9769.632134524423236);
      mercator_resolutions.put("Zoom  5 - 1:15 Million - 1250779.611 meters", 4885.857856551832811);
      mercator_resolutions.put("Zoom  6 - 1:10 Million - 625329.273 meters", 2442.692472795480171);
      mercator_resolutions.put("Zoom  7 - 1:4 Million - 312680.586 meters", 1221.408542870289011);
      mercator_resolutions.put("Zoom  8 - 1:2 Million - 156344.180 meters", 610.719455956439333);
      mercator_resolutions.put("Zoom  9 - 1:1 Million - 78173.049 meters", 305.363474400363145);
      mercator_resolutions.put("Zoom 10 - 1:500.000 - 39086.762 meters", 152.682667445300353);
      mercator_resolutions.put("Zoom 11 - 1:250.000 - 19543.440 meters", 76.341565429802998);
      mercator_resolutions.put("Zoom 12 - 1:150.000 - 9771.764 meters", 38.170956013236221);
      mercator_resolutions.put("Zoom 13 - 1:70.000 - 4885.886 meters", 19.085492344712762);
      mercator_resolutions.put("Zoom 14 - 1:35.000 - 2442.942 meters", 9.542742608409494);
      mercator_resolutions.put("Zoom 15 - 1:15.000 - 1221,471 meters", 4.771372073263216); // 1:17.500 ?
      mercator_resolutions.put("Zoom 16 - 1:8.000 - 610.735 meters", 2.385685756825894); 
      mercator_resolutions.put("Zoom 17 - 1:4.000 - 305.367 meters", 1.192842719290223); 
      mercator_resolutions.put("Zoom 18 - 1:2.000 - 152.683 meters", 0.596421364337474); 
      mercator_resolutions.put("Zoom 19 - 1:1.000 - 76.341 meters", 0.298210792187396); 
      mercator_resolutions.put("Zoom 20 - 1:500 - 38.170 meters", 0.149105396388009); 
      mercator_resolutions.put("Zoom 21 - 1:250 - 19.085 meters", 0.074552544930600); 
      mercator_resolutions.put("Zoom 22 - 1:125 - 9.542 meters", 0.037276174719086); 
      mercator_resolutions.put("Zoom 23 - 1:62.5 - 4.771 meters", 0.018638262558992);
      mercator_resolutions.put("Zoom 24 - 1:31.25 - 2.385 meters", 0.009319239432482);      
      mercator_resolutions.put("Zoom 25 - 1:16.125 - 1.192 meters", 0.004659619889283);
      mercator_resolutions.put("Zoom 26 - 1:8.0625 - 0.596 meters", 0.002329523079167);
      mercator_resolutions.put("Zoom 27 - 1:4.03125 - 0.298 meters", 0.001164761532319);
      mercator_resolutions.put("Zoom 28 - 1:2.015625 - 0.149 meters", 0.000582380761390);
      mercator_resolutions.put("Zoom 29 - 1:1.0078125 - 0.074 meters", 0.000291299252023);
      mercator_resolutions.put("Zoom 30 - 1:0.50390625 - 0.037 meters", 0.000145758555015);
      // Zoom 31: [gdalwarp] Failed to compute GCP transform: Transform is not solvable
     }  

    /**
     * Return zoom level of given resolution per pixel
     * - list is not sorted
     * -- values added from biggest to smallest
     * Goal: is to estimate what can be shown without being blank
     * @param resolution of image in 'WGS 84 / World Mercator' 3395
     * @return i_zoom_level were zoom resolution is smaller that the given resolution 
     */
     public static int getZoomlevelfromResolution(double resolution) {
      int i_zoom=0;
      for( Map.Entry<String, Double> view_entry : mercator_resolutions.entrySet() )
      {
       double zoom_resolution=view_entry.getValue();
       if (zoom_resolution < resolution)
				    return i_zoom;
       i_zoom++;
			   }
      return 0;
     }

    /**
     * Return zoom level text of given resolution per pixel
     * - list is not sorted
     * -- values added from biggest to smallest
     * Goal: is to estimate what can be shown without being blank
     * @param resolution of image in 'WGS 84 / World Mercator' 3395
     * @return zoom_level text were zoom resolution is smaller that the given resolution 
     */
     public static String getZoomlevelTextfromResolution(double resolution) {
      for( Map.Entry<String, Double> view_entry : mercator_resolutions.entrySet() )
      {
       double zoom_resolution=view_entry.getValue();
       if (zoom_resolution < resolution)
				    return view_entry.getKey();
			   }
      return "";
     }

    /**
     * Return info of Rasterlite2
     * - will be filled on first Database connection when empty
     * -- called in checkDatabaseTypeAndValidity
     * --- if this is empty, then the Driver has NOT been compiled for RasterLite2
     * '0.8;x86_64-linux-gnu'
     */
    public static String Rasterlite2Version_CPU = "";

    /**
     * Retrieve usable zoom levels from rasterlite2 resolutions.
     * <p/>
     * Note: RasterLite2 uses Pyramid logic and NOT min/max Zoom-Levels
     * - here we attemt to 'translate' the Pyramid high/low resolution to Zoom-Levels
     * - max_zoom can be upt to 30 [max 22 only seems to be supported by mapsforge]
     * - min_zoom must be calculated from the low_resolution value
     * -- a sql querey to retrieve this value is created in
     * --- RASTER_COVERAGES_QUERY_EXTENT_VALID_V42
     * <p/>
     * - the given 'vector_data' high value and sql will be replaced with min/max zoom.level
     * - the rebuild string to be used in SpatialiteDatabaseHandler.collectVectorTables for 'RasterLite2'
     * <p/>
     *
     * @param db the database to use.
     * @param vector_data data retrieved from RASTER_COVERAGES_QUERY_EXTENT_VALID_V42.
     * @return reformatted vector_data with zoom_levels
     */
    public static String getRasterlite2ResolutionZoomlevel(Database sqlite_db, String vector_data) {
        String vector_value=vector_data;
        double[] boundsCoordinates = new double[]{0.0, 0.0, 0.0, 0.0};
        double[] centerCoordinate = new double[]{0.0, 0.0};
        int i_min_zoom = 0;
        int i_max_zoom = 22;
        String[] sa_string = vector_data.split(";");
        // RGB;256;3068;0.476376793524109,SELECT max(x_resolution_1_8) FROM 'berlin_seiter.1846_levels';
        if (sa_string.length == 7) {
         String s_pixel_type = sa_string[0];
         String s_tile_width = sa_string[1];
         String s_srid = sa_string[2];
         String s_high_resolution = sa_string[3];
         // 3;22580.9594625996,18644.645565919,28245.0795376013,22989.6782996524;2014-07-29T09:50:25.740Z
         String s_num_bands = sa_string[4];
         String s_bounds = sa_string[5];
         String s_last_verified = sa_string[6];
         sa_string = s_high_resolution.split(",");
         s_high_resolution=sa_string[0];
         double high_resolution = Double.parseDouble(s_high_resolution);
         String s_select_low_resolution=sa_string[1];
         double low_resolution = 0.0;
         sa_string = s_bounds.split(",");
         if (sa_string.length == 4) {
          try {
               boundsCoordinates[0] = Double.parseDouble(sa_string[0]);
               boundsCoordinates[1] = Double.parseDouble(sa_string[1]);
               boundsCoordinates[2] = Double.parseDouble(sa_string[2]);
               boundsCoordinates[3] = Double.parseDouble(sa_string[3]);
              } catch (NumberFormatException e) {
           // ignore
          }
          // SELECT max(x_resolution_1_8) AS low_resolution FROM '''||coverage_name||'_levels'';
          if (s_select_low_resolution.contains("SELECT max(x_resolution_1_8) AS low_resolution"))
          {
           Stmt this_stmt = null;
           try {
                this_stmt = sqlite_db.prepare(s_select_low_resolution);
                if (this_stmt.step()) {
                    low_resolution = this_stmt.column_double(0);
                }
           } catch (jsqlite.Exception e_stmt) {
                int i_rc = sqlite_db.last_error();
                GPLog.error("DAOSPATIALIE", "getRasterlite2ResolutionZoomlevel sql[" + s_select_low_resolution + "] rc=" + i_rc + "]", e_stmt);
           } finally {
                // this_stmt.close();
           }
           if (low_resolution > 0)
           { // TODO: develop logic to calculate i_min_zoom level from known data
            // original image_width=11890 from SELECT width FROM 'berlin_seiter.1846_sections'
            // 28245,0795376013-22580,9594625996=5664,120075002
            // 5664,120075002/0,476376793524109=11890,000000001
            double raster_width = boundsCoordinates[2]-boundsCoordinates[0]; // 5664.1200750017015
            // Double.valueOf(
            int image_width=(int)(raster_width/high_resolution);
            SpatialiteUtilities.collectBoundsAndCenter(sqlite_db, s_srid, centerCoordinate, boundsCoordinates,"3395");
            // 5664,120075002/11890,000000001=0,476376794
            double high_resolution_mercator=(boundsCoordinates[2]-boundsCoordinates[0])/image_width;
            double low_resolution_mercator=high_resolution_mercator*(low_resolution/high_resolution);
            double pixel_resolution_mercator=low_resolution_mercator;///256;
            i_min_zoom=SPL_Rasterlite.getZoomlevelfromResolution(pixel_resolution_mercator);
            pixel_resolution_mercator=high_resolution_mercator;///256;
            String s_zoom_level_text=SPL_Rasterlite.getZoomlevelTextfromResolution(pixel_resolution_mercator);
            GPLog.androidLog(-1, "zoom_level_text["+s_zoom_level_text+"] select_low_resolution["+ s_select_low_resolution+"] low_resolution["+low_resolution+"] high_resolution["+high_resolution+"] raster_width["+raster_width+"] image_width["+image_width+"] mercator min_x["+ boundsCoordinates[0]+"] min_y["+boundsCoordinates[1]+"]  max_x["+boundsCoordinates[2]+"] max_y["+boundsCoordinates[3]+"] high_resolution_mercator["+high_resolution_mercator+"] low_resolution_mercator["+low_resolution_mercator+"] pixel_resolution_mercator["+pixel_resolution_mercator+"] i_min_zoom["+i_min_zoom+"]");
            // i_min_zoom-=5;
            if (i_min_zoom < 0)
             i_min_zoom=0;
// DaoSpatialite: select_low_resolution[SELECT max(x_resolution_1_8) AS low_resolution FROM 'berlin_seiter.1846_levels'] low_resolution[243.9049182843439] high_resolution[0.476376793524109] raster_width[5664.1200750017015] image_width[23]

// DaoSpatialite: mercator min_x[1488200.0266367528] min_y[6856145.897633106] max_x[1497509.0224857857] may_y[6863298.382440972] high_resolution_mercator[404.7389499579516]  low_resolution_mercator[0.0]
           }
          }
         }
         // rebuild string to be used in SpatialiteDatabaseHandler.collectVectorTables for 'RasterLite2'
         vector_value=s_pixel_type+";";
         vector_value+=s_tile_width+";";
         vector_value+=s_srid+";";
         String s_zoom_levels=i_min_zoom+","+i_max_zoom;
         vector_value+=s_zoom_levels+";";
         vector_value+=s_num_bands+";";
         vector_value+=s_bounds+";";
         vector_value+=s_last_verified+";";
        }
       return vector_value;
    }


    /**
     * Retrieve rasterlite2 image of a given bound and size.
     * <p/>
     * <p>https://github.com/geopaparazzi/Spatialite-Tasks-with-Sql-Scripts/wiki/RL2_GetMapImage
     *
     * @param db the database to use.
     * @param rasterTable the table to use.
     * @param tileBounds  [west,south,east,north] [minx, miny, maxx, maxy] bounds.
     * @param tileSize default 256 [Tile.TILE_SIZE].
     * @return the image data as byte[]
     */
    public static byte[] getRasterTileInBounds(Database db, AbstractSpatialTable rasterTable, double[] tileBounds, int tileSize) {

        byte[] bytes = SPL_Rasterlite.rl2_GetMapImageTile(db, rasterTable.getSrid(), rasterTable.getTableName(),
                tileBounds, tileSize);
        if (bytes != null) {
            return bytes;
        }
        return null;
    }

    /**
     * Retrieve rasterlite2 tile of a given bound [4326,wsg84] with the given size.
     * <p/>
     * https://github.com/geopaparazzi/Spatialite-Tasks-with-Sql-Scripts/wiki/RL2_GetMapImage
     *
     * @param sqlite_db    Database connection to use
     * @param destSrid     the destination srid (of the rasterlite2 image).
     * @param coverageName the table to use.
     * @param tileBounds   [west,south,east,north] [minx, miny, maxx, maxy] bounds.
     * @param i_tile_size  default 256 [Tile.TILE_SIZE].
     * @return the image data as byte[] as jpeg
     */
    public static byte[] rl2_GetMapImageTile(Database sqlite_db, String destSrid, String coverageName, double[] tileBounds,
                                             int i_tile_size) {
        return rl2_GetMapImage(sqlite_db, "4326", destSrid, coverageName, i_tile_size, i_tile_size, tileBounds,
                "default", "image/jpeg", "#ffffff", 0, 80, 1);
    }


    /**
     * Retrieve rasterlite2 image of a given bound and size.
     * - used by: SpatialiteUtilities.rl2_GetMapImageTile to retrieve tiles only
     * https://github.com/geopaparazzi/Spatialite-Tasks-with-Sql-Scripts/wiki/RL2_GetMapImage
     *
     * @param sqlite_db    Database connection to use
     * @param sourceSrid   the srid (of the n/s/e/w positions).
     * @param destSrid     the destination srid (of the rasterlite2 image).
     * @param coverageName the table to use.
     * @param width        of image in pixel.
     * @param height       of image in pixel.
     * @param tileBounds   [west,south,east,north] [minx, miny, maxx, maxy] bounds.
     * @param styleName    used in coverage. default: 'default'
     * @param mimeType     'image/tiff' etc. default: 'image/png'
     * @param bgColor      html-syntax etc. default: '#ffffff'
     * @param transparent  0 to 100 (?).
     * @param quality      0-100 (for 'image/jpeg')
     * @param reaspect     1 = adapt image width,height if needed based on given bounds
     * @return the image data as byte[]
     */
    public static byte[] rl2_GetMapImage(Database sqlite_db, String sourceSrid, String destSrid, String coverageName, int width,
                                         int height, double[] tileBounds, String styleName, String mimeType, String bgColor, int transparent, int quality,
                                         int reaspect) {
        boolean doTransform = false;
        if (!sourceSrid.equals(destSrid)) {
            doTransform = true;
        }
        // sanity checks
        if (styleName.equals(""))
            styleName = "default";
        if (mimeType.equals(""))
            mimeType = "image/png";
        if (bgColor.equals(""))
            bgColor = "#ffffff";
        if ((transparent < 0) || (transparent > 100))
            transparent = 0;
        if ((quality < 0) || (quality > 100))
            quality = 0;
        if ((reaspect < 0) || (reaspect > 1))
            reaspect = 1; // adapt image width,height if needed based on given bounds [needed for
        // tiles]
        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(tileBounds[0]);
        mbrSb.append(",");
        mbrSb.append(tileBounds[1]);
        mbrSb.append(",");
        mbrSb.append(tileBounds[2]);
        mbrSb.append(",");
        mbrSb.append(tileBounds[3]);
        if (doTransform) {
            mbrSb.append(",");
            mbrSb.append(sourceSrid);
            mbrSb.append("),");
            mbrSb.append(destSrid);
        }
        mbrSb.append(")");
        // SELECT
        // RL2_GetMapImage('berlin_postgrenzen.1890',BuildMBR(20800.0,22000.0,24000.0,19600.0),1200,1920,'default','image/png','#ffffff',0,0,1);
        String mbr = mbrSb.toString();
        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT RL2_GetMapImage('");
        qSb.append(coverageName);
        qSb.append("',");
        qSb.append(mbr);
        qSb.append(",");
        qSb.append(Integer.toString(width));
        qSb.append(",");
        qSb.append(Integer.toString(height));
        qSb.append(",'");
        qSb.append(styleName);
        qSb.append("','");
        qSb.append(mimeType);
        qSb.append("','");
        qSb.append(bgColor);
        qSb.append("',");
        qSb.append(Integer.toString(transparent));
        qSb.append(",");
        qSb.append(Integer.toString(quality));
        qSb.append(",");
        qSb.append(Integer.toString(reaspect));
        qSb.append(");");
        String s_sql_command = qSb.toString();
        Stmt this_stmt = null;
        byte[] ba_image = null;
        if (!SPL_Rasterlite.Rasterlite2Version_CPU.equals("")) { // only if rasterlite2 driver is active
            try {
                this_stmt = sqlite_db.prepare(s_sql_command);
                if (this_stmt.step()) {
                    ba_image = this_stmt.column_bytes(0);
                }
            } catch (jsqlite.Exception e_stmt) {
                /*
                  this internal lib error is not being caught and the application crashes
                  - the request was for a image 1/3 of the orignal size of 10607x8292 (3535x2764)
                  - big images should be avoided, since the application dies
                  'libc    : Fatal signal 11 (SIGSEGV) at 0x80c7a000 (code=1), thread 4216 (AsyncTask #2)'
                  '/data/app-lib/eu.hydrologis.geopaparazzi-2/libjsqlite.so (rl2_raster_decode+8248)'
                  'I WindowState: WIN DEATH: Window{41ee0100 u0 eu.hydrologis.geopaparazzi/eu.hydrologis.geopaparazzi.GeoPaparazziActivity}'
                */
                int i_rc = sqlite_db.last_error();
                GPLog.error("DAOSPATIALIE", "rl2_GetMapImage sql[" + s_sql_command + "] rc=" + i_rc + "]", e_stmt);
            } finally {
                // this_stmt.close();
            }
        }
        return ba_image;
    }


}
