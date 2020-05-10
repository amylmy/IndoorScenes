## Indoor Scenes

There are 160 categories of indoor scenes in **Places** and 67 categories in **MIT67** in total.

For **Places**, it contains both indoor and outdoor scenes. We extracted the indoor parts from the orignial dataset due to the label provided by the authors. More details about **Places** can be found [here](http://places2.csail.mit.edu/). And for MIT67, orignial data and labels can be found in [this website](http://web.mit.edu/torralba/www/indoor.html).

The two scene-centric datasets have 57 categories in common.

In this repository, files with same number in their names indicate overlapping categories contained by both **MIT67** and **Places**. Such as [names_mit_10.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_mit_10.txt) and [names_places_10.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_places_10.txt) contain same 10 categories of indoor scenes. The same for [names_mit_35.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_mit_35.txt) and [names_places_35.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_places_35.txt), etc. 

Other details and data will be updated in the future.


## Others

### Google Places API
- https://developers.google.com/places/supported_types

### Apple Indoor Map Data Format
- https://register.apple.com/resources/imdf/Categories/#venue
- Venue Types
  - Airport
  - Shopping Center
  - Train Station

### Floorplan
- Dataset
  - CubiCasa5k: https://github.com/CubiCasa/CubiCasa5k.git
  
  CubiCasa5K is a large-scale floorplan image dataset containing 5000 samples annotated into over 80 floorplan object categories. The dataset annotations are performed in a dense and versatile manner by using polygons for separating the different objects. 
  
- Papers
  - CubiCasa5K: A Dataset and an Improved Multi-Task Model for Floorplan Image Analysis
  - Parsing floor plan images
  - Deep floor plan recognition using a multi-task network with room-boundary-guided attention
