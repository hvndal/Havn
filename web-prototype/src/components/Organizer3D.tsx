import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';
import { OrganizerSlotData } from '../types';
import { soundManager } from '../audio/soundManager';

interface Organizer3DProps {
  slots: OrganizerSlotData[];
  selectedSlotIndex: number;
  onSelectSlot: (index: number) => void;
  isDark: boolean;
  className?: string;
}

function hexToInt(hex: string): number {
  return parseInt(String(hex || '#000000').replace('#', '0x'), 16);
}

function mixHex(a: string, b: string, t: number): string {
  const ai = hexToInt(a), bi = hexToInt(b);
  const ar = (ai >> 16) & 255, ag = (ai >> 8) & 255, ab = ai & 255;
  const br = (bi >> 16) & 255, bg = (bi >> 8) & 255, bb = bi & 255;
  const r = Math.round(ar + (br - ar) * t);
  const g = Math.round(ag + (bg - ag) * t);
  const bl = Math.round(ab + (bb - ab) * t);
  return '#' + [r, g, bl].map((v) => v.toString(16).padStart(2, '0')).join('');
}

export const Organizer3D: React.FC<Organizer3DProps> = ({
  slots,
  selectedSlotIndex,
  onSelectSlot,
  isDark,
  className = '',
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<THREE.Scene | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
  const boxGroupRef = useRef<THREE.Group | null>(null);
  const lidPivotsRef = useRef<THREE.Group[]>([]);
  const lidMeshesRef = useRef<THREE.Mesh[]>([]);
  const slotBaseMeshesRef = useRef<THREE.Mesh[]>([]);
  const pillGroupsRef = useRef<THREE.Group[]>([]);
  const openAnglesRef = useRef<number[]>([]);
  const targetAnglesRef = useRef<number[]>([]);
  const animFrameIdRef = useRef<number | null>(null);

  // Dragging state
  const isDraggingRef = useRef(false);
  const startPointerRef = useRef({ x: 0, y: 0 });
  const rotRef = useRef({ targetX: 0.35, targetY: -0.05, currentX: 0.35, currentY: -0.05, idleTime: 0 });

  const getThemePalette = (dark: boolean) => {
    return dark
      ? {
          shell: '#2A3029',
          shellSpecular: 0x4a544a,
          lid: '#333A31',
          slotIdle: '#242A23',
          slotDone: mixHex('#242A23', '#7B947B', 0.42),
          labelIdle: '#2C3329',
          labelCurrent: mixHex('#2C3329', '#7B947B', 0.26),
          labelSelected: mixHex('#2C3329', '#7B947B', 0.38),
          labelDone: mixHex('#2C3329', '#7B947B', 0.55),
          labelBorder: '#48513F',
          labelBorderDone: '#7B947B',
          labelText: '#EDEDEA',
          labelTextSoft: '#A3A89F',
          ambient: 0x8f9a8c,
          ambientIntensity: 0.7,
          key: 0xdfe6d8,
          keyIntensity: 0.8,
          fill: 0x5d7a60,
          fillIntensity: 0.4,
          shadowOpacity: 0.3,
        }
      : {
          shell: '#DCE3DA',
          shellSpecular: 0xffffff,
          lid: '#FFFFFF',
          slotIdle: '#DBDAD6',
          slotDone: '#C5DAC3',
          labelIdle: '#F2EFE9',
          labelCurrent: '#E5EFE4',
          labelSelected: '#D3E6D1',
          labelDone: '#C8DEC6',
          labelBorder: '#ADA89E',
          labelBorderDone: '#445744',
          labelText: '#2D3B2D',
          labelTextSoft: '#828680',
          ambient: 0xfffdfa,
          ambientIntensity: 0.75,
          key: 0xfffdf5,
          keyIntensity: 1.0,
          fill: 0x7c947b,
          fillIntensity: 0.45,
          shadowOpacity: 0.2,
        };
  };

  const createLidTexture = (slot: OrganizerSlotData, isSelected: boolean, dark: boolean) => {
    const canvas = document.createElement('canvas');
    canvas.width = 128;
    canvas.height = 256;
    const ctx = canvas.getContext('2d');
    if (!ctx) return new THREE.CanvasTexture(canvas);

    const p = getThemePalette(dark);
    const isCompleted = slot.isCompleted;
    const isCurrent = slot.isCurrent;

    // Background
    if (isCompleted) ctx.fillStyle = p.labelDone;
    else if (isSelected) ctx.fillStyle = p.labelSelected;
    else if (isCurrent) ctx.fillStyle = p.labelCurrent;
    else ctx.fillStyle = p.labelIdle;
    ctx.fillRect(0, 0, 128, 256);

    // Outer border
    ctx.strokeStyle = isCompleted ? p.labelBorderDone : p.labelBorder;
    ctx.lineWidth = 6;
    ctx.strokeRect(3, 3, 122, 250);

    // Period label
    ctx.fillStyle = p.labelText;
    ctx.font = '600 20px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    const labelText = slot.label || 'SLOT';
    const tracked = labelText.split('').join(' ');
    ctx.fillText(tracked, 64, 100, 116);

    // Subtitle
    ctx.font = '14px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    ctx.fillStyle = isCompleted ? p.labelText : p.labelTextSoft;

    const pillCount = (slot.pills && slot.pills.length) || 0;
    if (isCompleted) {
      ctx.fillText('✓ Done', 64, 145);
    } else {
      ctx.fillText(pillCount > 0 ? `${pillCount} ${pillCount === 1 ? 'dose' : 'doses'}` : 'Empty', 64, 145);
    }

    const texture = new THREE.CanvasTexture(canvas);
    texture.needsUpdate = true;
    return texture;
  };

  const createPillMesh = (pillData: any) => {
    const pGroup = new THREE.Group();
    const hexColor = hexToInt(pillData.color || '#516351');
    const secHexColor = hexToInt(pillData.secondaryColor || '#FBFBFA');
    const shape = pillData.shape || 'capsule';

    if (shape === 'softgel') {
      const geo = new THREE.SphereGeometry(0.065, 20, 14);
      const mat = new THREE.MeshPhongMaterial({
        color: hexColor,
        transparent: true,
        opacity: 0.85,
        shininess: 85,
        specular: 0xffffff,
      });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.scale.set(1.0, 1.35, 0.85);
      mesh.castShadow = true;
      pGroup.add(mesh);
    } else if (shape === 'tablet') {
      const geo = new THREE.CylinderGeometry(0.075, 0.075, 0.045, 20);
      const mat = new THREE.MeshPhongMaterial({
        color: hexColor,
        shininess: 24,
        specular: 0xffffff,
      });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.castShadow = true;

      const lineGeo = new THREE.BoxGeometry(0.13, 0.01, 0.008);
      const lineMat = new THREE.MeshBasicMaterial({ color: 0x555555 });
      const line = new THREE.Mesh(lineGeo, lineMat);
      line.position.y = 0.023;
      mesh.add(line);
      pGroup.add(mesh);
    } else {
      // Two-tone capsule
      const cap1Geo = new THREE.CylinderGeometry(0.055, 0.055, 0.1, 16);
      const cap1Mat = new THREE.MeshPhongMaterial({ color: hexColor, shininess: 30 });
      const cap1 = new THREE.Mesh(cap1Geo, cap1Mat);
      cap1.position.y = 0.05;
      cap1.castShadow = true;

      const cap2Geo = new THREE.CylinderGeometry(0.055, 0.055, 0.1, 16);
      const cap2Mat = new THREE.MeshPhongMaterial({ color: secHexColor, shininess: 30 });
      const cap2 = new THREE.Mesh(cap2Geo, cap2Mat);
      cap2.position.y = -0.05;
      cap2.castShadow = true;

      const sphere1 = new THREE.Mesh(new THREE.SphereGeometry(0.055, 16, 8), cap1Mat);
      sphere1.position.y = 0.1;
      sphere1.castShadow = true;

      const sphere2 = new THREE.Mesh(new THREE.SphereGeometry(0.055, 16, 8), cap2Mat);
      sphere2.position.y = -0.1;
      sphere2.castShadow = true;

      pGroup.add(cap1);
      pGroup.add(cap2);
      pGroup.add(sphere1);
      pGroup.add(sphere2);
    }

    pGroup.rotation.z = Math.PI / 2 + (Math.random() * 0.2 - 0.1);
    pGroup.rotation.x = 0.25;
    return pGroup;
  };

  // Rebuild organizer meshes
  const rebuildBox = () => {
    const boxGroup = boxGroupRef.current;
    if (!boxGroup) return;

    while (boxGroup.children.length > 0) {
      boxGroup.remove(boxGroup.children[0]);
    }

    slotBaseMeshesRef.current = [];
    lidPivotsRef.current = [];
    lidMeshesRef.current = [];
    pillGroupsRef.current = [];

    const numSlots = slots.length;
    openAnglesRef.current = Array(numSlots).fill(0);
    targetAnglesRef.current = Array(numSlots).fill(0);
    if (selectedSlotIndex >= 0 && selectedSlotIndex < numSlots) {
      targetAnglesRef.current[selectedSlotIndex] = 1;
    }

    const slotWidth = numSlots === 4 ? 0.58 : 0.46;
    const boxWidth = numSlots * slotWidth + 0.28;
    const palette = getThemePalette(isDark);

    // Body
    const bodyGeo = new THREE.BoxGeometry(boxWidth, 0.55, 1.35);
    const bodyMat = new THREE.MeshPhongMaterial({
      color: hexToInt(palette.shell),
      shininess: 32,
      specular: palette.shellSpecular,
    });
    const bodyMesh = new THREE.Mesh(bodyGeo, bodyMat);
    bodyMesh.castShadow = true;
    bodyMesh.receiveShadow = true;
    boxGroup.add(bodyMesh);

    const startX = -((numSlots - 1) * slotWidth) / 2;

    for (let i = 0; i < numSlots; i++) {
      const xPos = startX + i * slotWidth;
      const slotData = slots[i];

      // Base marker
      const slotGeo = new THREE.BoxGeometry(slotWidth - 0.08, 0.04, 1.1);
      const slotMat = new THREE.MeshBasicMaterial({
        color: hexToInt(slotData.isCompleted ? palette.slotDone : palette.slotIdle),
      });
      const slotMesh = new THREE.Mesh(slotGeo, slotMat);
      slotMesh.position.set(xPos, 0.28, 0);
      slotMesh.userData = { slotIndex: i };
      slotMesh.receiveShadow = true;
      boxGroup.add(slotMesh);
      slotBaseMeshesRef.current.push(slotMesh);

      // Pill group
      const pGroup = new THREE.Group();
      pGroup.position.set(xPos, 0.32, 0);
      boxGroup.add(pGroup);
      pillGroupsRef.current.push(pGroup);

      // Pills inside
      const pills = slotData.pills || [];
      const count = pills.length;
      for (let p = 0; p < count; p++) {
        const pillData = pills[p];
        const pillMesh = createPillMesh(pillData);
        const xOffset = (p - (count - 1) / 2) * 0.13;
        const yOffset = p * 0.02;
        const zOffset = (p % 2) * 0.08 - 0.04;
        pillMesh.position.set(xOffset, yOffset, zOffset);
        pillMesh.userData = {
          pillId: pillData.id,
          targetScale: pillData.isTaken ? 0.001 : 1.0,
        };
        if (pillData.isTaken) {
          pillMesh.scale.set(0.001, 0.001, 0.001);
          pillMesh.visible = false;
        } else {
          pillMesh.scale.set(1, 1, 1);
          pillMesh.visible = true;
        }
        pGroup.add(pillMesh);
      }

      // Hinged Lid Pivot
      const pivot = new THREE.Group();
      pivot.position.set(xPos, 0.32, -0.62);

      const lidGeo = new THREE.BoxGeometry(slotWidth - 0.08, 0.08, 1.25);
      const labelTex = createLidTexture(slotData, i === selectedSlotIndex, isDark);
      const lidMat = new THREE.MeshPhongMaterial({
        map: labelTex,
        color: 0xffffff,
        shininess: 16,
      });
      const lidMesh = new THREE.Mesh(lidGeo, lidMat);
      lidMesh.position.set(0, 0.04, 0.625);
      lidMesh.userData = { slotIndex: i };
      lidMesh.castShadow = true;
      lidMesh.receiveShadow = true;

      pivot.add(lidMesh);
      boxGroup.add(pivot);

      lidPivotsRef.current.push(pivot);
      lidMeshesRef.current.push(lidMesh);
    }
  };

  // Initialize Three.js scene
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const width = container.clientWidth || 400;
    const height = container.clientHeight || 320;

    const scene = new THREE.Scene();
    sceneRef.current = scene;

    const camera = new THREE.PerspectiveCamera(38, width / height, 0.1, 1000);
    camera.position.set(0, 3.2, 5.5);
    camera.lookAt(0, 0, 0);
    cameraRef.current = camera;

    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    rendererRef.current = renderer;

    container.innerHTML = '';
    container.appendChild(renderer.domElement);

    const palette = getThemePalette(isDark);

    // Lights
    const ambientLight = new THREE.AmbientLight(palette.ambient, palette.ambientIntensity);
    scene.add(ambientLight);

    const dirLight = new THREE.DirectionalLight(palette.key, palette.keyIntensity);
    dirLight.position.set(4, 9, 5);
    dirLight.castShadow = true;
    dirLight.shadow.mapSize.width = 1024;
    dirLight.shadow.mapSize.height = 1024;
    scene.add(dirLight);

    const fillLight = new THREE.DirectionalLight(palette.fill, palette.fillIntensity);
    fillLight.position.set(-4, -1, -3);
    scene.add(fillLight);

    // Ground Shadow
    const shadowGeo = new THREE.PlaneGeometry(12, 12);
    const shadowMat = new THREE.ShadowMaterial({ opacity: palette.shadowOpacity });
    const shadowPlane = new THREE.Mesh(shadowGeo, shadowMat);
    shadowPlane.rotation.x = -Math.PI / 2;
    shadowPlane.position.y = -0.28;
    shadowPlane.receiveShadow = true;
    scene.add(shadowPlane);

    // Box Group
    const boxGroup = new THREE.Group();
    boxGroup.rotation.x = 0.35;
    boxGroup.rotation.y = -0.05;
    scene.add(boxGroup);
    boxGroupRef.current = boxGroup;

    rebuildBox();

    // Render loop
    const animate = () => {
      animFrameIdRef.current = requestAnimationFrame(animate);

      const rot = rotRef.current;
      rot.idleTime += 0.005;
      if (!isDraggingRef.current) {
        rot.targetY += 0.0008;
        rot.targetX = 0.35 + Math.sin(rot.idleTime) * 0.02;
      }

      boxGroup.rotation.y += (rot.targetY - boxGroup.rotation.y) * 0.08;
      boxGroup.rotation.x += (rot.targetX - boxGroup.rotation.x) * 0.08;

      // Lid Hinges Animation
      const pivots = lidPivotsRef.current;
      const targetAngles = targetAnglesRef.current;
      const openAngles = openAnglesRef.current;
      for (let i = 0; i < pivots.length; i++) {
        const target = (targetAngles[i] || 0) * (-Math.PI * 0.55);
        openAngles[i] = (openAngles[i] || 0) + (target - (openAngles[i] || 0)) * 0.15;
        if (pivots[i]) pivots[i].rotation.x = openAngles[i];
      }

      // Pill scale interpolation
      pillGroupsRef.current.forEach((pGroup) => {
        pGroup.children.forEach((pillMesh) => {
          const targetS = pillMesh.userData.targetScale !== undefined ? pillMesh.userData.targetScale : 1.0;
          pillMesh.scale.x += (targetS - pillMesh.scale.x) * 0.15;
          pillMesh.scale.y += (targetS - pillMesh.scale.y) * 0.15;
          pillMesh.scale.z += (targetS - pillMesh.scale.z) * 0.15;
          pillMesh.visible = pillMesh.scale.x >= 0.02;
        });
      });

      renderer.render(scene, camera);
    };

    animate();

    const resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const { width: w, height: h } = entry.contentRect;
        if (w > 0 && h > 0 && cameraRef.current && rendererRef.current) {
          cameraRef.current.aspect = w / h;
          cameraRef.current.updateProjectionMatrix();
          rendererRef.current.setSize(w, h);
        }
      }
    });

    resizeObserver.observe(container);

    return () => {
      resizeObserver.disconnect();
      if (animFrameIdRef.current) {
        cancelAnimationFrame(animFrameIdRef.current);
      }
      renderer.dispose();
      container.innerHTML = '';
    };
  }, []);

  // Update when slots or theme changes
  useEffect(() => {
    rebuildBox();
  }, [slots, isDark]);

  // Update target lid angles on selection change
  useEffect(() => {
    if (targetAnglesRef.current) {
      targetAnglesRef.current = slots.map((_, idx) => (idx === selectedSlotIndex ? 1 : 0));
    }
  }, [selectedSlotIndex, slots]);

  // Pointer interaction handlers
  const handlePointerDown = (e: React.PointerEvent) => {
    isDraggingRef.current = false;
    startPointerRef.current = { x: e.clientX, y: e.clientY };
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    const dx = e.clientX - startPointerRef.current.x;
    const dy = e.clientY - startPointerRef.current.y;

    if (Math.abs(dx) > 4 || Math.abs(dy) > 4) {
      isDraggingRef.current = true;
      rotRef.current.targetY += dx * 0.005;
      rotRef.current.targetX = Math.max(0.1, Math.min(0.75, rotRef.current.targetX + dy * 0.003));
      startPointerRef.current = { x: e.clientX, y: e.clientY };
    }
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    if (!isDraggingRef.current) {
      // Raycast click detection
      const container = containerRef.current;
      const camera = cameraRef.current;
      const boxGroup = boxGroupRef.current;
      if (!container || !camera || !boxGroup) return;

      const rect = container.getBoundingClientRect();
      const mouse = new THREE.Vector2(
        ((e.clientX - rect.left) / rect.width) * 2 - 1,
        -((e.clientY - rect.top) / rect.height) * 2 + 1
      );

      const raycaster = new THREE.Raycaster();
      raycaster.setFromCamera(mouse, camera);

      const intersects = raycaster.intersectObjects(
        [...lidMeshesRef.current, ...slotBaseMeshesRef.current, ...boxGroup.children],
        true
      );

      if (intersects.length > 0) {
        for (const hit of intersects) {
          if (hit.object.userData && hit.object.userData.slotIndex !== undefined) {
            const hitIndex = hit.object.userData.slotIndex;
            soundManager.playCeramicClick();
            onSelectSlot(hitIndex);
            break;
          }
        }
      }
    }
  };

  const resetAngle = () => {
    rotRef.current.targetX = 0.35;
    rotRef.current.targetY = -0.05;
    soundManager.playSoftTap();
  };

  return (
    <div className={`relative w-full h-full select-none ${className}`}>
      <div
        ref={containerRef}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        className="w-full h-full cursor-grab active:cursor-grabbing three-canvas-wrap touch-none"
      />

      {/* Floating 3D controls hint & reset */}
      <div className="absolute bottom-2 right-3 flex items-center gap-2 pointer-events-auto">
        <button
          onClick={resetAngle}
          title="Reset perspective"
          className="px-2.5 py-1 text-[11px] font-medium tracking-wide uppercase rounded-md bg-[#FFFFFF]/70 dark:bg-[#242822]/70 backdrop-blur-xs text-[#5E645A] dark:text-[#A3A89F] hover:text-[#141613] dark:hover:text-[#EDEDEA] border border-[#E2E1D9]/50 dark:border-[#292E26]/50 transition-colors shadow-2xs"
        >
          Reset View
        </button>
      </div>

      <div className="absolute top-2 left-3 pointer-events-none">
        <span className="text-[10px] font-medium tracking-wider uppercase text-[#8C9287] dark:text-[#73796E] bg-[#FFFFFF]/60 dark:bg-[#141613]/60 px-2 py-0.5 rounded backdrop-blur-xs border border-[#E2E1D9]/40 dark:border-[#292E26]/40">
          3D Interactive · Drag to rotate · Tap to open
        </span>
      </div>
    </div>
  );
};
