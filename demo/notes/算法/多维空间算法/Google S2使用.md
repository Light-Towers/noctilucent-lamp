```bash
pip install s2sphere matplotlib shapely cartopy
```



```python
import s2sphere as s2
import matplotlib.pyplot as plt
from shapely.geometry import Polygon
import cartopy.crs as ccrs
import cartopy.io.img_tiles as cimgt


proj = cimgt.OSM()
plt.figure(figsize=(20, 20), dpi=200)
ax = plt.axes(projection=proj.crs)
ax.add_image(proj, 12)

ax.set_extent([-51.411886, -50.922470,
-30.301314, -29.94364])

region_rect = s2.LatLngRect(
s2.LatLng.from_degrees(-51.264871, -30.241701),
s2.LatLng.from_degrees(-51.04618, -30.000003))
coverer = s2.RegionCoverer()
coverer.min_level=8
coverer.max_level=15
coverer.max_cells=500
covering = coverer.get_covering(region_rect)
geoms = []

for cellid in covering:
    new_cell = s2.Cell(cellid)
    vertices = []
    for i in range(0, 4):
        vertex = new_cell.get_vertex(i)
        latlng = s2.LatLng.from_point(vertex)
        vertices.append((latlng.lat().degrees,latlng.lng().degrees))

    geo = Polygon(vertices)
    geoms.append(geo)

print("Total Geometries: {}".format(len(geoms)))

ax.add_geometries(geoms, ccrs.PlateCarree(), facecolor='coral', edgecolor='black', alpha=0.4)
plt.show()
```

# 问题
`cartopy` 包安装会遇到一些问题，linux解决方法：

```bash
sudo apt -y install libgeos-dev
```

macOS暂未找到解决的方法，所以无法调试。