If you like what I do and want to support me, you can

<a href="https://www.buymeacoffee.com/cosminradu" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

# CropMarker

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fcodevblocks%2Fandroid%2Fcropmarker%2Fmaven-metadata.xml)
![Java](https://img.shields.io/badge/language-java-10748d.svg)
![Android](https://img.shields.io/badge/platform-android-green.svg)

A crop marker overlay view with support to show, adjust and compute a crop area. The crop marker is defined by a rectangle but can be visually represented by either a `rectangle` or an `oval` [`mask`](https://github.com/CoDevBlocks/CropMarker#mask). The size (see [`minSize`](https://github.com/CoDevBlocks/CropMarker#minSize)) and position (see [`left`](https://github.com/CoDevBlocks/CropMarker#left), [`top`](https://github.com/CoDevBlocks/CropMarker#top), [`right`](https://github.com/CoDevBlocks/CropMarker#right), [`bottom`](https://github.com/CoDevBlocks/CropMarker#bottom)) of the rectangle can be modified by dragging its 4 corner touch handles (see [`touchThreshold`](https://github.com/CoDevBlocks/CropMarker#touchThreshold), [`touchHandleDrawable`](https://github.com/CoDevBlocks/CropMarker#touchHandleDrawable) and all `touchHandle*` XML attributes) or by dragging the rectangle itself. Visually, the touch handles are drawn as two perpendicular lines forming a 90 degrees angle, but custom drawables can also be used. Optionally, horizontal and vertical grid lines can be specified (see [`gridLines`](https://github.com/CoDevBlocks/CropMarker#gridLines), [`gridLinesBehavior`](https://github.com/CoDevBlocks/CropMarker#gridLinesBehavior)). By default, the marker rectangle can be resized freely, but it can also be limited to a width/height aspect ratio (see [`aspectRatio`](https://github.com/CoDevBlocks/CropMarker#aspectRatio)).

![CropMarker Sample 1](/media/cropmarker_01.gif)
![CropMarker Sample 1](/media/cropmarker_02.gif)

## Installation

Simply add the dependency to your module `build.gradle` file

```groovy
dependencies {
    // ...
    implementation 'com.codevblocks.android:cropmarker:<version>'
    // ...
}
```
Current version available on [Maven](https://search.maven.org/artifact/com.codevblocks.android/cropmarker)

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fcodevblocks%2Fandroid%2Fcropmarker%2Fmaven-metadata.xml)

## Usage

Include the `com.codevblocks.android.cropmarker.CropMarker` view in your XML layout file and make sure it is overlayed on top of the image view. Use `getCropBounds()` to retrieve the RELATIVE ([0..1]) crop rectangle. Here is a complete XML example:

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.appcompat.widget.AppCompatImageView
        android:id="@+id/image"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"

        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"

        android:src="@drawable/motorcycle"
        android:scaleType="fitCenter"
        android:adjustViewBounds="true" />

    <com.codevblocks.android.cropmarker.CropMarker
        android:id="@+id/crop_marker"
        android:layout_width="0dp"
        android:layout_height="0dp"

        app:layout_constraintTop_toTopOf="@id/image"
        app:layout_constraintBottom_toBottomOf="@id/image"
        app:layout_constraintStart_toStartOf="@id/image"
        app:layout_constraintEnd_toEndOf="@id/image"

        app:enabled="true"

        app:minSize="50dp"
        app:aspectRatio="0"

        app:left="0dp"
        app:top="0dp"
        app:right="150dp"
        app:bottom="250dp"

        app:mask="rectangle"
        app:overlayColor="#D0FFFFFF"

        app:markerStrokeColor="@android:color/white"
        app:markerStrokeWidth="1dp"
        app:markerStrokeDashWidth="2dp"
        app:markerStrokeDashGap="2dp"

        app:touchHandleDrawable="@null"
        app:touchHandleDrawableAnchorX="0%"
        app:touchHandleDrawableAnchorY="0%"
        app:touchHandleStrokeColor="#B0343A"
        app:touchHandleStrokeWidth="4dp"
        app:touchHandleStrokeLength="16dp"
        app:touchHandleStrokeInset="4dp"
        app:touchThreshold="30dp"

        app:gridLinesBehavior="touch"
        app:gridLines="2"
        app:gridLinesColor="#80FFFFFF"
        app:gridLinesWidth="1dp"
        app:gridLinesDashWidth="0dp"
        app:gridLinesDashGap="0dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

## XML Attributes

#### `enabled`
Determines if the crop marker is shown.
```
app:enabled="true|false"
```
#### `minSize`
The minimum width & height of the crop marker rectangle
```
app:minSize="50dp"
```
#### `aspectRatio`
The width/height aspect ratio to be enforced on the crop marker rectangle. If equal to 0 or `Float.NaN`, the rectangle is freely resizable
```
app:aspectRatio="0"
```
#### `left`
The initial left position of the crop marker rectangle
```
app:left="0dp"
```
#### `top`
The initial top position of the crop marker rectangle
```
app:top="0dp"
```
#### `right`
The initial right position of the crop marker rectangle
```
app:right="150dp"
```
#### `bottom`
The initial bottm position of the crop marker rectangle
```
app:bottom="250dp"
```
#### `mask`
The shape of the crop rectangle inner mask. Can be either a `rectangle` or an `oval`
```
app:mask="rectangle|oval"
```
#### `overlayColor`
The overlay color outside of the crop marker rectangle
```
app:overlayColor="#D0FFFFFF"
```
#### `markerStrokeColor`
The crop marker rectangle stroke color
```
app:markerStrokeColor="@android:color/white"
```
#### `markerStrokeWidth`
The crop marker rectangle stroke width
```
app:markerStrokeWidth="1dp"
```
#### `markerStrokeDashWidth`
The dash length if the crop marker rectangle stroke is a dashed line. In order for the line to be dashed, both  `markerStrokeDashWidth` and `markerStrokeDashGap` need to be greater than 0.
```
app:markerStrokeDashWidth="2dp"
```
#### `markerStrokeDashGap`
The dash gap length if the crop marker rectangle stroke is a dashed line. In order for the line to be dashed, both  `markerStrokeDashWidth` and `markerStrokeDashGap` need to be greater than 0.
```
app:markerStrokeDashGap="2dp"
```
#### `touchHandleDrawable`
The crop marker rectangle corner touch handle drawable
```
app:touchHandleDrawable="@null"
```
```
app:touchHandleDrawable="@drawable/drawable_touch_handle"
```
#### `touchHandleDrawableAnchorX`
The reference X position (horizontal) point within the touch handle drawable.
```
app:touchHandleDrawableAnchorX="50%"
```
#### `touchHandleDrawableAnchorY`
The reference Y position (vertical) point within the touch handle drawable.
```
app:touchHandleDrawableAnchorY="50%"
```
#### `touchHandleStrokeColor`
The default touch handle stroke color
```
app:touchHandleStrokeColor="#B0343A"
```
#### `touchHandleStrokeWidth`
The default touch handle stroke width
```
app:touchHandleStrokeWidth="4dp"
```
#### `touchHandleStrokeLength`
The default touch handle stroke length
```
app:touchHandleStrokeLength="16dp"
```
#### `touchHandleStrokeInset`
The default touch handle stroke inset. A positive value offsets the touch handle stroke towards the inside of the crop marker rectangle.
```
app:touchHandleStrokeInset="4dp"
```
#### `touchThreshold`
The horizontal and vertical threshold from the crop marker rectangle corner point which determines if a touch handle is grabbed. A higher value increases the the touch handle area.
```
app:touchThreshold="30dp"
```
#### `gridLinesBehavior`
Determines when the grid lines should be shown: `never`, `always` or only upon crop marker `touch`.
```
app:gridLinesBehavior="none|touch|always"
```
#### `gridLines`
The number of horizontal & vertical grid lines
```
app:gridLines="2"
```
#### `gridLinesColor`
The grid lines color
```
app:gridLinesColor="#80FFFFFF"
```
#### `gridLinesWidth`
The grid line stroke width
```
app:gridLinesWidth="1dp"
```
#### `gridLinesDashWidth`
The dash length if the grid line stroke is dashed. In order for the grid line to be dashed, both  `gridLinesDashWidth` and `gridLinesDashGap` need to be greater than 0.
```
app:gridLinesDashWidth="0dp"
```
#### `gridLinesDashGap`
The dash gap length if the grid line stroke is dashed. In order for the grid line to be dashed, both  `gridLinesDashWidth` and `gridLinesDashGap` need to be greater than 0.
```
app:gridLinesDashGap="0dp"
```