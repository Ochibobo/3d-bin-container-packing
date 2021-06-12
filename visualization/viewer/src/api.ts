import * as THREE from "three";
import { Color, Mesh, Object3D, Scene } from "three";
import randomColor from "randomcolor";

export class Stackable {

    dx : number;
    dy : number;
    dz : number;

    name: string;
    id: string;

    constructor(name : string, id : string, dx : number, dy : number, dz: number) {

        this.name = name;
        this.id = id;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

}

export class Box extends Stackable {

    constructor(name : string, id : string, dx : number, dy : number, dz: number) {
        super(name, id, dx, dy, dz);
    }
    
}

export class Container extends Stackable {

    loadDx : number;
    loadDy : number;
    loadDz : number;

    stack : Stack

    constructor(name : string, id : string, dx : number, dy : number, dz: number, loadDx : number, loadDy : number, loadDz: number) {
        super(name, id, dx, dy, dz);

        this.loadDx = loadDx;
        this.loadDy = loadDy;
        this.loadDz = loadDz;

        this.stack = new Stack();
    }
    
    public add(stackPlacement : StackPlacement) {
        this.stack.add(stackPlacement);
    }
}

export class StackPlacement {

    stackable : Stackable;
    x : number;
    y : number;
    z : number;

    constructor(stackable : Stackable, x : number, y : number, z: number) {
        this.stackable = stackable;
        this.x = x;
        this.y = y;
        this.z = z;
    }

}

export class Stack {

    placements : Array<StackPlacement>;

    constructor() {
        this.placements = new Array();
    }

    public add(placement : StackPlacement) {
        this.placements.push(placement);
    }

}

export class StackPlacementControls {

    stackPlacement : StackPlacement;
    object3d : Object3D;
    scene : Scene;

    constructor(scene: Scene, object3d : Object3D, stackPlacement : StackPlacement) {
        this.scene = scene;
        this.object3d = object3d;
        this.stackPlacement = stackPlacement;
    }
}

export interface ColorScheme {
    getColor(stackable : Stackable) : Color;
    getColorScheme(container : Container) : ColorScheme;
}

export class RandomColorScheme implements ColorScheme {
    getColor(stackable : Stackable) : Color {
        return new Color(randomColor());
    }

    getColorScheme(container : Container) : ColorScheme {
        return this;
    }

}

export class MemoryColorScheme implements ColorScheme {

    delegate : ColorScheme;
    map : Map<string, Color>;

    constructor(delegate : ColorScheme) {
        this.delegate = delegate;
        this.map = new Map();
    }

    getColor(stackable : Stackable) : Color {
        if(!stackable.id) {
            // use random
            return this.delegate.getColor(stackable);
        }
        // use same as before, for the same id
        var color = this.map.get(stackable.id);
        if(!color) {
            color = this.delegate.getColor(stackable);
            this.map.set(stackable.id, color);
        }
        return color;
    }

    getColorScheme(container : Container) : ColorScheme {
        return this;
    }

}

export class StackableRenderer {

    add(parent: Object3D, colorScheme : ColorScheme, stackPlacement : StackPlacement, x: number, y:number, z: number): Object3D | undefined {

        var stackable = stackPlacement.stackable;

        if(stackable instanceof Container) {
            var containerStackable = stackable as Container;

            var color = colorScheme.getColor(containerStackable);
            var containerMaterial = new THREE.LineBasicMaterial({ color: color});
            var containerGeometry = new THREE.EdgesGeometry(new THREE.BoxGeometry(containerStackable.dx, containerStackable.dy, containerStackable.dz));

            var containerLoadGeometry = new THREE.EdgesGeometry(new THREE.BoxGeometry(containerStackable.loadDx, containerStackable.loadDy, containerStackable.loadDz));

            var container = new THREE.LineSegments(containerGeometry, containerMaterial);
            var containerLoad = new THREE.LineSegments(containerLoadGeometry, containerMaterial);

            container.position.x = stackPlacement.x + containerStackable.dx / 2 + x;
            container.position.y = stackPlacement.y + containerStackable.dy / 2 + y;
            container.position.z = stackPlacement.z + containerStackable.dz / 2 + z;

            console.log("Add container " + containerStackable.name + " size " + containerStackable.dx + "x" + containerStackable.dy + "x" + containerStackable.dz + " with load " + containerStackable.loadDx + "x" + containerStackable.loadDy + "x" + containerStackable.loadDz + " at " + stackPlacement.x + "x" + stackPlacement.y + "x" + stackPlacement.z) ;

            parent.add(container);
            container.add(containerLoad);

            var offsetX = - containerStackable.dx / 2;
            var offsetY = - containerStackable.dy / 2;
            var offsetZ = - containerStackable.dz / 2;

            var nextColorScheme = colorScheme.getColorScheme(containerStackable);
            for (let s of containerStackable.stack.placements) {
                this.add(containerLoad, nextColorScheme, s, offsetX, offsetY, offsetZ);
            }

            return container;
        } else if(stackable instanceof Box) {
            var boxStackable = stackable as Box;

            console.log("Add box " + boxStackable.name + " size " + boxStackable.dx + "x" + boxStackable.dy + "x" + boxStackable.dz + " at " + stackPlacement.x + "x" + stackPlacement.y + "x" + stackPlacement.z);

            var sColor = colorScheme.getColor(boxStackable);

            var material = new THREE.MeshStandardMaterial({
                color: sColor,
                opacity: 0.7,
                metalness: 0.2,
                roughness: 1,
                transparent: true,
                polygonOffset: true,
                polygonOffsetFactor: 1,
                polygonOffsetUnits: 1
            });
            var geometry = new THREE.BoxGeometry(1, 1, 1);
            var box = new THREE.Mesh(geometry, material);

            box.scale.x = boxStackable.dx;
            box.scale.y = boxStackable.dy;
            box.scale.z = boxStackable.dz;
            box.position.x = stackPlacement.x + boxStackable.dx / 2 + x;
            box.position.y = stackPlacement.y + boxStackable.dy / 2 + y;
            box.position.z = stackPlacement.z + boxStackable.dz / 2 + z;

            parent.add(box);

            return box;
        }
        return undefined;
    }
}    

