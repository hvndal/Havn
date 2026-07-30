---
name: Warm Minimalism
colors:
  surface: '#fbf9f5'
  surface-dim: '#dbdad6'
  surface-bright: '#fbf9f5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3ef'
  surface-container: '#efeeea'
  surface-container-high: '#eae8e4'
  surface-container-highest: '#e4e2de'
  on-surface: '#1b1c1a'
  on-surface-variant: '#434842'
  inverse-surface: '#30312e'
  inverse-on-surface: '#f2f0ed'
  outline: '#747872'
  outline-variant: '#c3c8c0'
  surface-tint: '#516351'
  primary: '#516351'
  on-primary: '#ffffff'
  primary-container: '#8da08c'
  on-primary-container: '#263727'
  inverse-primary: '#b8ccb6'
  secondary: '#8a4f33'
  on-secondary: '#ffffff'
  secondary-container: '#feb28f'
  on-secondary-container: '#794227'
  tertiary: '#735b23'
  on-tertiary: '#ffffff'
  tertiary-container: '#b49759'
  on-tertiary-container: '#423000'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d4e8d2'
  primary-fixed-dim: '#b8ccb6'
  on-primary-fixed: '#0f1f11'
  on-primary-fixed-variant: '#3a4b3b'
  secondary-fixed: '#ffdbcc'
  secondary-fixed-dim: '#ffb694'
  on-secondary-fixed: '#351000'
  on-secondary-fixed-variant: '#6d391e'
  tertiary-fixed: '#ffdf9c'
  tertiary-fixed-dim: '#e2c381'
  on-tertiary-fixed: '#251a00'
  on-tertiary-fixed-variant: '#59440d'
  background: '#fbf9f5'
  on-background: '#1b1c1a'
  surface-variant: '#e4e2de'
typography:
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '500'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '500'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
  title-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  margin-edge: 24px
  gutter: 16px
  stack-lg: 48px
  stack-md: 24px
  stack-sm: 12px
---

## Brand & Style

The design system is rooted in the philosophy of "A daily ritual, beautifully kept." It bridges the gap between digital utility and physical wellness objects, drawing heavy inspiration from Scandinavian industrial design and editorial lifestyle publications. The aesthetic is defined by **Warm Minimalism**: a focus on essentialism without the coldness typically associated with modern tech.

The interface prioritizes psychological comfort, using matte textures, diffused lighting, and organic shapes to reduce the "medical" anxiety often associated with medication management. The goal is to evoke the sensation of touching a smooth ceramic surface or flipping through a premium art magazine—quiet, thoughtful, and high-end.

## Colors

The palette is a curated selection of "earth-tones" that mimic natural materials. 

- **Canvas:** The primary background is Warm Ivory (#FAF8F4), providing a softer, more organic foundation than pure white.
- **Accents:** Muted Sage (#8DA08C) serves as the primary action color, chosen for its psychological links to nature and healing. Terracotta and Soft Butter Yellow are used sparingly for status differentiation (e.g., specific pill types or urgent reminders) without breaking the tranquil mood.
- **Neutrals:** Gradients of Sand and Stone create a sense of physical layering. All text uses a deep Charcoal rather than black to maintain a soft contrast ratio that is easy on the eyes.

## Typography

This design system uses **Hanken Grotesk** (as a high-quality alternative to Satoshi) to achieve a modern, editorial look that remains highly legible for all age groups. 

- **Editorial Hierarchy:** Headers are intentionally large with tight letter-spacing to create a "masthead" feel on every screen. 
- **Readability:** Body text is set with generous line heights (1.5x+) to ensure instructions and medication names are effortless to scan.
- **Metadata:** Small labels use increased letter-spacing and a semi-bold weight to remain distinct even at small scales, appearing like fine print on high-end packaging.

## Layout & Spacing

The layout philosophy is based on **Extreme Whitespace**. Components are never crowded; they are given "room to breathe," which translates to a sense of calm for the user.

- **Grid:** A standard 12-column fluid grid for tablet/desktop, and a 4-column grid for mobile.
- **Safe Zones:** A mandatory 24px outer margin ensures content never feels pinched by the device frame.
- **Rhythm:** Vertical spacing follows a 4px baseline, but primary sections are separated by "Stack LG" (48px) to clearly define different areas of the "ritual."

## Elevation & Depth

Hierarchy is established through **Ambient Depth** rather than traditional drop shadows. 

- **Matte Surfaces:** Cards use a very slight inner glow and a wide, low-opacity (8-10%) shadow that mimics light hitting a physical matte-finished plastic or ceramic object.
- **Tonal Layering:** Depth is primarily communicated through color shifts. The background canvas is the lowest layer, while active cards sit on a "Stone" or "Sand" colored surface to feel lifted.
- **Softness:** Shadows are never black; they are always tinted with a hint of the background "Stone" color to maintain a natural look.

## Shapes

The shape language is defined by **Generous Radii**. There are no sharp corners in the design system.

- **Container Rounds:** Primary cards and containers use a minimum of 24px (rounded-lg) to 32px (rounded-xl) corners.
- **Interactive Elements:** Buttons and input fields use pill-shaped (fully rounded) geometry to invite touch and feel "squishy" and friendly.
- **Visual Metaphor:** Shapes should echo the form factors of industrial design objects—rounded rectangles that feel like they were molded from a single piece of material.

## Components

### Tactile Cards
The primary vessel for information. Cards feature a subtle 1px border in a slightly darker shade of the neutral palette and a soft shadow. They should feel like physical objects resting on the ivory background.

### Minimalist Buttons
Buttons use the Muted Sage as a fill for primary actions. The compression state should be a subtle scale-down (98%) and a slight darkening of the color, providing haptic-like visual feedback without being jarring.

### Virtual Pill Organizer
The signature component. It is a 3D-esque, top-down view of a physical case. It uses smooth gradients and soft shadows to look like a premium lifestyle product. Individual "slots" should have high-definition pill icons that feel like product photography rather than abstract glyphs.

### Input Fields & Controls
Floating inputs with no bottom line; instead, they sit inside a soft "Sand" colored pill-shaped container. Switches and checkboxes should move with a slow, dampened animation to maintain the "quiet" brand personality.

### Custom Iconography
Line icons with a consistent 2pt stroke and rounded ends. Medication icons (capsules, tablets) should have a slight "3D" shading to match the virtual organizer’s realism.