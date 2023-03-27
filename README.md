## Indoor Scenes

There are 160 categories of indoor scenes in **Places** and 67 categories in **MIT67** in total.

For **Places**, it contains both indoor and outdoor scenes. We extracted the indoor parts from the original dataset based on the label provided by the authors. More details about **Places** can be found [here](http://places2.csail.mit.edu/). And for MIT67, original data and labels can be found in [this website](http://web.mit.edu/torralba/www/indoor.html).

The two scene-centric datasets have 57 categories in common.

In this repository, files with the same number in their names indicate overlapping categories contained by both **MIT67** and **Places**. Such as [names_mit_10.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_mit_10.txt) and [names_places_10.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_places_10.txt) contain same 10 categories of indoor scenes. The same for [names_mit_35.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_mit_35.txt) and [names_places_35.txt](https://github.com/amylmy/IndoorScenes-UnsupervidedRepresentationLearning/blob/master/names_places_35.txt), etc. 

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

### Floorplan (2D)
- 2020 | Indoor mapping and modeling by parsing floor plan images | [[paper](https://www.tandfonline.com/doi/full/10.1080/13658816.2020.1781130)]
- 2019 | ICCV | Deep floor plan recognition using a multi-task network with room-boundary-guided attention | [[paper](https://openaccess.thecvf.com/content_ICCV_2019/papers/Zeng_Deep_Floor_Plan_Recognition_Using_a_Multi-Task_Network_With_Room-Boundary-Guided_ICCV_2019_paper.pdf)] [[project](https://github.com/zlzeng/DeepFloorplan)]
- 2019 | CubiCasa5K: A Dataset and an Improved Multi-Task Model for Floorplan Image Analysis | [[paper](https://arxiv.org/abs/1904.01920v1)] [[project](https://github.com/CubiCasa/CubiCasa5k)]
  - CubiCasa5K is a large-scale floorplan image dataset containing 5000 samples annotated into over 80 floorplan object categories. The dataset annotations are performed in a dense and versatile manner by using polygons for separating the different objects.
- 2017 | ICCV | Raster-to-Vector: Revisiting Floorplan Transformation | [[paper](http://art-programmer.github.io/floorplan-transformation/paper.pdf)] [[project](https://github.com/art-programmer/FloorplanTransformation)]
- 2017 | Parsing floor plan images | [[paper](https://ieeexplore.ieee.org/abstract/document/7986875)]
- 2013 | Statistical segmentation and structural recognition for floor plan interpretation | [[paper](https://link.springer.com/article/10.1007/s10032-013-0215-2)]
- [CVC-FP: Database for structural floor plan analysis](http://dag.cvc.uab.es/resources/floorplans/)


### Indoor Structure (3D)
- 2021 | ICCV | 3D-FRONT: 3D Furnished Rooms With layOuts and semaNTics | [[paper](https://openaccess.thecvf.com/content/ICCV2021/html/Fu_3D-FRONT_3D_Furnished_Rooms_With_layOuts_and_semaNTics_ICCV_2021_paper.html) [project](https://tianchi.aliyun.com/specials/promotion/alibaba-3d-scene-dataset)]
- 2021 | CVPR | Plan2Scene: Converting floorplans to 3D scenes | [[paper](https://openaccess.thecvf.com/content/CVPR2021/papers/Vidanapathirana_Plan2Scene_Converting_Floorplans_to_3D_Scenes_CVPR_2021_paper.pdf)] [[code](https://github.com/3dlg-hcvc/plan2scene)]
- 2019 | ICCV | Floor-SP: Inverse CAD for Floorplans by Sequential Room-wise Shortest Path | [[paper](https://openaccess.thecvf.com/content_ICCV_2019/html/Chen_Floor-SP_Inverse_CAD_for_Floorplans_by_Sequential_Room-Wise_Shortest_Path_ICCV_2019_paper.html)] [[project](http://jcchen.me/floor-sp/)] [[code](https://github.com/woodfrog/floor-sp)]
- 2019 | ICCV | Floorplan-Jigsaw: Jointly Estimating Scene Layout and Aligning Partial Scans | [[paper](https://openaccess.thecvf.com/content_ICCV_2019/html/Lin_Floorplan-Jigsaw_Jointly_Estimating_Scene_Layout_and_Aligning_Partial_Scans_ICCV_2019_paper.html)]
- 2016 | CVPR | 3D Semantic Parsing of Large-Scale Indoor Spaces | [[paper](https://www.cv-foundation.org/openaccess/content_cvpr_2016/html/Armeni_3D_Semantic_Parsing_CVPR_2016_paper.html)] 
- 2015 | ICCV | Structured Indoor Modeling | [[paper](https://www.cv-foundation.org/openaccess/content_iccv_2015/html/Ikehata_Structured_Indoor_Modeling_ICCV_2015_paper.html)] [[project](https://www2.cs.sfu.ca/~furukawa/sim/)]
- 2015 | CVPR | Rent3D: Floor-Plan Priors for Monocular Layout Estimation | [[paper](http://www.cs.toronto.edu/~fidler/papers/rent3DCVPR15.pdf)] [[project](http://www.cs.toronto.edu/~fidler/projects/rent3D.html)]

### Scene Generation
- 2023 | TPAMI | SceneHGC: Hierarchical Graph Networks for 3D Indoor Scene Generation with Fine-Grained Geometry | [[paper](https://arxiv.org/pdf/2302.10237.pdf)] [[project](http://geometrylearning.com/scenehgn/)]
