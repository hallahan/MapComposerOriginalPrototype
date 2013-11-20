# Geo599 - Algorithms for Geovisualization Project Specification

Nicholas Hallahan, Fall 2013

## Purpose

The purpose of my project is to refine and extend MapComposer so that it can
function as a stand-alone web application allowing the user to create pseudo-
natural maps of any location on Earth from a web interface.

## Milestones

I would like to split the project into three phases:

### Phase 1

In addition to consuming imagery and image masks as an individual local file
or a local directory with tiles, I will be adding the functionality to consume
any remote image or tile set via http. This means that MapComposer will be able
to use masks and composite imagery from any http source.

To make the tool useful, I have set up a TileMill server that provides tiles
to function as binary masks for producing a given layer in MapComposer. This
server can also produce tiles of orthoimagery as well as an arbitrary tiled
basemap than can be used as a layer in MapComposer. An installer script will be
provided that does all of the necessary work of importing OpenStreetMap data
into PostGIS and properly connecting PostGIS to TileMill.

### Phase 2

Rather than simply batch rendering the pseudo-natural map to PNG tiles locally, 
I will be creating a Java servlet that provides a REST API 
that generates a given tile with given parameters by an HTTP request.

This will provide the groundwork for creating an HTML5 application that allows
the user to define and view a pseudo-natural tile set dynamically within the
browser.

### Phase 3

I will create an HTML5 application that provides similar functionality seen in
the Java SWING user interface. The pseudo-natural map will be rendered on the
fly as the user changes parameters. These generated tiles will be seen in a 
Leaflet Map.

## Schedule

Phase 1: Wed Dec 4 2013

Phase 2: Wed Dec 18 2013

Phase 3: Wed Jan 1 2014

## Input / Output Data Types and Formats

### Input

**Binary masks** will continue to be used to define the pixels that a corresponding
layer will be rendered to. The pixels in the mask PNGS will function as a boolean
flag. Black is on, white is off. 

## Code Structure

## UML

## Potential Difficulties
