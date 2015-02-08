package cwru.edu.cwrumap;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CWRUMapActivity extends FragmentActivity {

    // We don't use namespaces
    static final String ns = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cwrumap);
        setUpMapIfNeeded();
        try {
            addAllMarkers();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(R.menu.menu, menu);
//        return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_sethybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.menu_showtraffic:
                mMap.setTrafficEnabled(true);
                break;

            case R.id.menu_zoomin:
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;

            case R.id.menu_zoomout:
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
        }
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(41.50364,  -81.60784)).title("Rocke").snippet("RockeFeller Building"));
        CameraPosition cp = new CameraPosition(new LatLng(41.50364,  -81.60784), 18, 0, 0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    /**
     * Adds a marker to the map
     */
    private void addMarker(String title, String snippet, double lat, double lon, String categ){


        BitmapDescriptor bd;
        switch (categ) {
            case "building": bd = BitmapDescriptorFactory.fromResource(R.drawable.schoolpng);
                break;
            //"Map symbol parking 02" by seamus mcgill (mcgill) - Own work.
            // Licensed under Public Domain via Wikimedia Commons -
            // http://commons.wikimedia.org/wiki/File:Map_symbol_parking_02.png#mediaviewer/
            // File:Map_symbol_parking_02.png
            case "lot": bd = BitmapDescriptorFactory.fromResource(R.drawable.parking);
                break;
            case "entrance": bd = BitmapDescriptorFactory.fromResource(R.drawable.exitrance);
                break;
            default: bd = null;
        }
        /** Make sure that the map has been initialised **/
        if(null != mMap){
            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title(title)
                            .snippet(snippet)
                            .draggable(false)
                                    // choose icon based on category
                            .icon(bd)
            );
        }
    }

    /**
     * Adds all text markers to the map
     */
    private void addAllMarkers() throws IOException, XmlPullParserException {

        /** Make sure that the map has been initialised **/
        if(null != mMap){
            XmlPullParser xpp = getResources().getXml(R.xml.names);
            List<Entry> entries;
            try {
                entries = readFeed(xpp);
                for (Entry e:entries) {
                    addMarker(e.title, e.snippet, e.lat, e.lon, e.categ);
                }
            }
            catch (XmlPullParserException e){
            } catch (IOException e) {
            }
        }
    }

    public static class Entry {
        public final String title;
        public final String snippet;
        public final double lat;
        public final double lon;
        public final String categ;

        private Entry(String title, String snippet, double lat, double lon, String categ) {
            this.title = title;
            this.snippet = snippet;
            this.lat = lat;
            this.lon = lon;
            this.categ = categ;
        }
    }

    /**
     * Makes a List of Entries from the parser that have associated data.
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        int next = parser.next();
        next = parser.next();
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "feed");
        while (next != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                next = parser.next();
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                //skip(parser);
            }
            next = parser.nextTag();
        }
        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, snippet, lat, or lon tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private CWRUMapActivity.Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "entry");
        String title = null;
        String snippet = null;
        double lat = 0;
        double lon = 0;
        String categ = null;
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("snippet")) {
                snippet = readSnippet(parser);
            } else if (name.equals("lat")) {
                lat = readLat(parser);
            } else if (name.equals("lon")) {
                lon = readLon(parser);
            } else if (name.equals("categ")) {
                categ = readCateg(parser);
            } else {
                //skip(parser);
            }
        }
        return new CWRUMapActivity.Entry(title, snippet, lat, lon, categ);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "title");
        String title = readText(parser);
        //parser.require(XmlPullParser.END_TAG, CWRUMapActivity.ns, "title");
        return title;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Processes link tags in the feed.
    private String readSnippet(XmlPullParser parser) throws IOException, XmlPullParserException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "title");
        String snippet = readText(parser);
        //parser.require(XmlPullParser.END_TAG, CWRUMapActivity.ns, "title");
        return snippet;
    }

    // Processes lat tags in the feed.
    private double readLat(XmlPullParser parser) throws IOException, XmlPullParserException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "lat");
        double lat = readDouble(parser);
        //parser.require(XmlPullParser.END_TAG, CWRUMapActivity.ns, "lat");
        return lat;
    }

    // Processes lon tags in the feed.
    private double readLon(XmlPullParser parser) throws IOException, XmlPullParserException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "lon");
        double lon = readDouble(parser);
        //parser.require(XmlPullParser.END_TAG, CWRUMapActivity.ns, "lon");
        return lon;
    }

    // For the tags title and summary, extracts their text values.
    private double readDouble(XmlPullParser parser) throws IOException, XmlPullParserException {
        double result = 0;
        if (parser.next() == XmlPullParser.TEXT) {
            result = Double.parseDouble(parser.getText());
            parser.next();
        }
        return result;
    }

    private String readCateg(XmlPullParser parser) throws IOException, XmlPullParserException {
        //parser.require(XmlPullParser.START_TAG, CWRUMapActivity.ns, "title");
        String categ = readText(parser);
        //parser.require(XmlPullParser.END_TAG, CWRUMapActivity.ns, "title");
        return categ;
    }

   /*
    * Unused because it skips things we don't use. Left in for usability in the future.
    */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
