License
=======
Copyright (C) dumptruckman 2013

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at http://mozilla.org/MPL/2.0/.

Gradle Dependency Info
=====================
```
repositories {
    maven {
        name = 'onarandombox'
        url = uri('https://repo.onarandombox.com/content/groups/public/')
    }
}

dependencies {
    implementation 'com.dumptruckman.minecraft:JsonConfiguration:1.2'
    // Required json dependency because no other json lib can handle numbers appropriately.
    implementation 'net.minidev:json-smart:2.5.1'
}
```

Maven Dependency Info
=====================
```xml
<repositories>
    <repository>
        <id>onarandombox</id>
        <url>https://repo.onarandombox.com/content/groups/public</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.dumptruckman.minecraft</groupId>
        <artifactId>JsonConfiguration</artifactId>
        <version>1.2</version>
    </dependency>
    <!-- Required json dependency because no other json lib can handle numbers appropriately. -->
    <dependency>
        <groupId>net.minidev</groupId>
        <artifactId>json-smart</artifactId>
        <version>2.5.1</version>
    </dependency>
</dependencies>
```
