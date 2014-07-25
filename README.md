okulus(BETA)
============
Custom Imageview for Android that allows for setting shapes/borders/shadows efficiently. This is currently in Beta as some minor issues are getting ironed out.

Demo app is coming on the Play Store soon!

The basic concept for drawing Bitmaps with a shape without creating a new Bitmap is based on [Romain Guy](https://plus.google.com/+RomainGuy)'s article on drawing [Bitmaps with Rounded Corners](http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/).

### Usage
1. Clone the repository and add the library to your project.

2. Add the custom namespace attribute to your layout 
```xml
xmlns:okulus="http://schemas.android.com/apk/res/com.vinaysshenoy.okulus
```

3. Add `OkulusImageView` to your layout and set the attributes you need
```xml
<com.vinaysshenoy.okulus.OkulusImageView
            android:id="@+id/image_1"
            android:layout_width="128dp"
            android:layout_height="96dp"
            okulus:fullCircle="true"
            okulus:borderWidth="2dp"
            okulus:borderColor="#FF0000"
            okulus:shadowRadius="0.05"
            okulus:shadowWidth="1.5dp"
            okulus:shadowColor="#00FF00"
            />
```

4. Call `setImageBitmap()` and you're done!
```java
OkulusImageView imageView = findViewById(R.id.image_1);
imageView.setImageBitmap(bitmap);
```

### Custom Attributes
1. `cornerRadius(dimension)` - Sets the corner radius used for adding the rounded corners. Set it to 50% of the width(for a square image) to make it a full circle. Default `0dp`.
2. `fullCircle(boolean)` - If this is set to `true`, the entire Bitmap will be drawn as a circle. The width and height will be set to whichever is smaller among them, and `cornerRadius` attribute will be ignored. Default `false`.
3. `borderWidth(dimension)` - Sets the width of the border to be drawn. Will be capped at `5dp`.
4. `borderColor(color)` - Sets the color of the border to draw. Default color is `#FF000000`.
5. `shadowWidth(dimension)` - Sets the width of the shadow to draw. Will be capped at `3dp`.
6. `shadowColor(color)` - Sets the color of the shadow to draw. Default is `#B3444444`.
7. `shadowRadius(float)` - Defines how sharp the shadow is. Set it to a small value(~0.5) for sharp shadows, and larger values for soft shadows. Default `0.5`
8. `touchSelectorColor(color)` - Defines the colour of the color overlayed on the view when it is touched. This is ignored if `touchSelectorEnabled` is `false`. Default `#66444444`
9. `touchSelectorEnabled(boolean)` - Defines whether the touch selectors should be drawn or not. Default is `false`

### Pros
1. No extra memory used for creating the reshaped Bitmap
2. Zero overdraw
3. Any combination of shapes - Rounded Rects, Rects, Squares, Circles are possible with borders + shadow
 
### Limitations
1. Supports only fixed dimensions. `wrap_content` cannot be used.
2. Does not respect the `scaleType` attribute of `ImageView`. Scaled Bitmaps need to be provided.
3. Shadows cannot be used without borders
4. Supports only the `setImageBitmap` method.
5. Attributes can only be set through XML
6. Shadows are currently drawn to the right and bottom of the View and cannot be changed.

## Roadmap
### Version 1.0
1. Adding `get()` and `set()` attributes for `OkulusImageView` for setting attributes through code
2. Respecting ImageView's `scaleType` attribute
 
### Future(in descending order of priority)
1. Adding support for `wrap_content`
2. Adding support for any configuration of shadows
3. Adding support for `setImageUri`, `setImageResource` and `setImageDrawable`
4. Adding support for color filters to easily configure effects like Sepia, Grayscale etc.
5. Adding support for Image transitions when changing the image content
6. Adding support for custom shapes
7. ?

### License
Copyright 2014 Vinay S Shenoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
