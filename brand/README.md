# Launcher icons

1. Go to [Android Asset Studio][asset-studio-launcher-icons]

2. To generate the square icons, use these settings:
    - Foreground: Image (foreground.svg)
    - Trim whitespace: Trim
    - Padding: 20%
    - Color: Transparent
    - Background color: #9c27b0
    - Scaling: Center
    - Shape: Square
    - Effect: Cast shadow
    - Name: ic_launcher

3. Similarly, to generate round icons, use these settings:
    - Foreground type: Image (foreground.svg)
    - Trim whitespace: Trim
    - Padding: 25%
    - Color: Transparent
    - Background color: #9c27b0
    - Scaling: Center
    - Shape: Circle
    - Effect: Cast shadow
    - Name: ic_launcher_round


# Feature graphic

To generate, visit [this link][feature-graphic-generator] and select foreground.svg as the image.


# Splash icon

To generate, visit [SVG to Android Vector Drawable converter][svg2vector] and select foreground.svg.


[asset-studio-launcher-icons]: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
[feature-graphic-permalink]: https://www.norio.be/android-feature-graphic-generator/?config=%7B%22background%22%3A%7B%22color%22%3A%22%239c27b0%22%2C%22gradient%22%3A%7B%22type%22%3A%22none%22%2C%22radius%22%3A%22660%22%2C%22angle%22%3A%22vertical%22%2C%22color%22%3A%22%23000000%22%7D%7D%2C%22title%22%3A%7B%22text%22%3A%22Kotlin%20K%C5%8Dans%22%2C%22position%22%3A148%2C%22color%22%3A%22%23ffffff%22%2C%22size%22%3A188%2C%22font%22%3A%7B%22family%22%3A%22Inconsolata%22%2C%22effect%22%3A%22bold%22%7D%7D%2C%22subtitle%22%3A%7B%22text%22%3A%22Learn%20Kotlin%20on%20the%20go%22%2C%22color%22%3A%22%23ffffff%22%2C%22size%22%3A100%2C%22offset%22%3A0%2C%22font%22%3A%7B%22family%22%3A%22Inconsolata%22%2C%22effect%22%3A%22normal%22%7D%7D%2C%22image%22%3A%7B%22position%22%3A%220.15%22%2C%22positionX%22%3A%220.5%22%2C%22scale%22%3A%221%22%2C%22file%22%3A%7B%7D%7D%2C%22size%22%3A%22feature-graphic%22%7D
[svg2vector]: http://inloop.github.io/svg2android/
