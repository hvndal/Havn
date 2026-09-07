# Havn Medication Visual Library

## 1. Overview & Core Philosophy

Havn is designed to support users in maintaining their daily medication and wellness rituals with elegance, psychological comfort, and clarity. Visual representations within Havn serve an **aesthetic and organizational purpose** within the application—helping users visually distinguish between different medications in their daily schedule—and are **NOT an identification or verification system**.

### Key Visual Principles & Disclaimers

1. **Non-Exact Generic Representations**: Actual physical medication appearance (color, shape, imprint, size, coating) varies significantly by manufacturer, distributor, generic formulation, dose strength, and country of origin. Havn explicitly **does not** represent any medication as having a single, universally correct physical pill appearance.
2. **Tasteful Aesthetic Visual System**: Medications in Havn are paired with generic visual representations drawn from Havn's design system tokens (e.g., soft sage, terracotta, slate, butter amber, sand tones with capsule, tablet, liquid, powder, or injection forms).
   - *Example*: Sertraline → generic capsule/tablet visual selected from the visual system.
   - *Example*: Fluoxetine → generic capsule/tablet visual selected from the visual system.
   - *Example*: Escitalopram → generic tablet visual selected from the visual system.
3. **No Scraped Photography or Copyrighted Imagery**: Havn does not scrape images from Google or copy copyrighted pharmaceutical product photography. All visual elements are programmatically rendered vectors, design system tokens, or public domain metadata representations.
4. **Authoritative Medical Metadata**: Where medication metadata is needed, Havn relies on authoritative medication naming systems such as **RxNorm** (U.S. National Library of Medicine) and official labeling sources such as **DailyMed** (FDA drug labeling).
5. **Clear User Guidance**: The app explicitly clarifies to users that displayed visuals are aesthetic representations for visual organization in their schedule, not an exact representation of a user's prescription or a physical pill identifier.

---

## 2. Visual System Parameters & Design Tokens

Havn's visual library maps each medication to standardized visual parameters defined in the Havn Design System.

### Visual Types (`visual_type`)
- **`Capsule`**: Two-tone or solid elongated capsule vector (`MedIconType.CAPSULE`).
- **`Tablet`**: Round or oval solid pill vector (`MedIconType.TABLET`).
- **`Liquid`**: Bottle / droplet icon vector (`MedIconType.LIQUID`).
- **`Powder`**: Packet / sachet icon vector (`MedIconType.POWDER`).
- **`Injection`**: Prefilled syringe / pen icon vector (`MedIconType.INJECTION`).

### Color Tags & Palette Tokens (`color_tag`)
- **`sage`** (`#516351` / `#8DA08C`): Calm botanical green, default for wellness & daily maintenance.
- **`terracotta`** (`#8A4F33` / `#D18B6A`): Warm earth tone, used for specialized or acute medications.
- **`butter`** (`#735B23` / `#EBCB88`): Soft warm amber, used for morning vitamins & supplements.
- **`slate`** (`#546363` / `#9BAEB5`): Muted cool blue-grey, used for evening or calming medications.
- **`sand`** (`#787268` / `#BEB09A`): Neutral warm stone, used for digestive and general health.

### Visual Parameter Attributes (`visual_parameters`)
- **`shape`**: `Capsule (Two-tone)`, `Tablet (Round)`, `Tablet (Oval)`, `Tablet (Oblong)`, `Liquid (Dropper/Bottle)`, `Powder (Sachet)`, `Injection (Pen/Vial)`.
- **`primary_color`**: Design token key (`sage`, `terracotta`, `butter`, `slate`, `sand`).
- **`accent_color`**: Secondary highlight token for multi-tone visuals.
- **`finish`**: `Matte Ceramic`, `Soft Dual-Tone`, `Translucent Liquid`.
- **`icon_glyph`**: Vector resource (`ic_med_capsule`, `ic_med_tablet`, `ic_med_liquid`, `ic_med_powder`, `ic_med_injection`).

---

## 3. Extensibility Architecture

The Medication Visual Library is structured to allow seamless expansion without requiring redesigns of the application.

```
                    ┌────────────────────────────┐
                    │ User Input / Search Term   │
                    └─────────────┬──────────────┘
                                  │
                                  ▼
                   ┌──────────────────────────────┐
                   │  RxNorm Concept Lookup       │
                   │  (RxCUI & Semantic Category) │
                   └─────────────┬────────────────┘
                                  │
                                  ▼
                   ┌──────────────────────────────┐
                   │  Havn Category Visual Matcher│
                   └─────────────┬────────────────┘
                                  │
                                  ▼
                   ┌──────────────────────────────┐
                   │ Generic Visual Token Mapping │
                   │ (Visual Type + Color Tag)    │
                   └──────────────────────────────┘
```

1. **Mapping Engine**: A fallback hierarchy resolves medication names to visual tokens:
   - **Exact Match**: Direct RxCUI / Drug Name -> Visual Preset.
   - **Category Match**: Anatomical Therapeutic Chemical (ATC) / RxNorm Class -> Default Category Visual Preset.
   - **Form Factor Match**: Dosage Form (e.g. "Oral Tablet", "Oral Capsule", "Oral Solution") -> Form Visual Type.
   - **Default Fallback**: `Capsule` + `sage`.
2. **Schema Decoupling**: Medical metadata (RxNorm RxCUI, Generic Name, ATC Category) is strictly decoupled from Visual Rendering Tokens (Type, Color, Shape), allowing future visual updates or additional categories without affecting clinical data structures.

---

## 4. Initial Medication Visual Catalog

Below is the initial visual catalog for Havn, covering major common medication categories. All entries represent **tasteful generic visual representations**.

### 4.1 SSRIs (Selective Serotonin Reuptake Inhibitors)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Zoloft** | Sertraline hydrochloride | Tablet / Capsule | `shape`: Oval Tablet / Capsule<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 203239), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Prozac** | Fluoxetine hydrochloride | Capsule / Tablet | `shape`: Two-tone Capsule<br>`primary_color`: `sage`<br>`accent_color`: `slate`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 216031), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Lexapro** | Escitalopram oxalate | Tablet | `shape`: Round Tablet<br>`primary_color`: `sage`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 352741), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Celexa** | Citalopram hydrobromide | Tablet | `shape`: Oval Tablet<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 252804), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Paxil** | Paroxetine hydrochloride | Tablet | `shape`: Round Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 198080), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.2 SNRIs (Serotonin-Norepinephrine Reuptake Inhibitors)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Effexor XR** | Venlafaxine hydrochloride | Capsule | `shape`: Extended-Release Capsule<br>`primary_color`: `terracotta`<br>`accent_color`: `sand`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 352085), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Cymbalta** | Duloxetine hydrochloride | Capsule | `shape`: Delayed-Release Capsule<br>`primary_color`: `slate`<br>`accent_color`: `sage`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 596928), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Pristiq** | Desvenlafaxine succinate | Tablet | `shape`: Square/Round Extended-Release<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 792825), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.3 ADHD Medications
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Ritalin / Concerta** | Methylphenidate hydrochloride | Tablet / Capsule | `shape`: Round Tablet / Oblong ER<br>`primary_color`: `butter`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 303350), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Vyvanse** | Lisdexamfetamine dimesylate | Capsule | `shape`: Two-tone Capsule<br>`primary_color`: `terracotta`<br>`accent_color`: `butter`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 727402), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Adderall / Adderall XR** | Amphetamine / Dextroamphetamine | Tablet / Capsule | `shape`: Round Tablet / Capsule<br>`primary_color`: `butter`<br>`accent_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 213169), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Strattera** | Atomoxetine hydrochloride | Capsule | `shape`: Two-tone Capsule<br>`primary_color`: `slate`<br>`accent_color`: `sand`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 358253), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.4 Blood Pressure Medications (Antihypertensives)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Zestril / Prinivil** | Lisinopril | Tablet | `shape`: Round Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 29046), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Norvasc** | Amlodipine besylate | Tablet | `shape`: Octagonal / Oval Tablet<br>`primary_color`: `sage`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 17767), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Cozaar** | Losartan potassium | Tablet | `shape`: Tear / Oval Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 5224), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Lopressor / Toprol-XL** | Metoprolol succinate / tartrate | Tablet | `shape`: Round Scored Tablet<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 6918), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.5 Cholesterol Medications (Statins)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Lipitor** | Atorvastatin calcium | Tablet | `shape`: Elliptical / Oval Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 83367), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Zocor** | Simvastatin | Tablet | `shape`: Oval Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 36567), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Crestor** | Rosuvastatin calcium | Tablet | `shape`: Round Tablet<br>`primary_color`: `sage`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 301542), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.6 Diabetes Medications
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Glucophage** | Metformin hydrochloride | Tablet | `shape`: Large Round / Oblong Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 6809), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Jardiance** | Empagliflozin | Tablet | `shape`: Oval / Round Tablet<br>`primary_color`: `butter`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 1545653), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Januvia** | Sitagliptin phosphate | Tablet | `shape`: Round Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 637188), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Ozempic / Wegovy** | Semaglutide | Injection | `shape`: Prefilled Injector Pen<br>`primary_color`: `sage`<br>`accent_color`: `slate`<br>`finish`: Smooth Matte Pen<br>`icon`: `ic_med_injection` | RxNorm (RxCUI: 1991302), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.7 Thyroid Medications
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Synthroid / Levoxyl** | Levothyroxine sodium | Tablet | `shape`: Round Small Tablet<br>`primary_color`: `butter`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 10582), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Cytomel** | Liothyronine sodium | Tablet | `shape`: Round Small Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 6423), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.8 Allergy Medications (Antihistamines)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Zyrtec** | Cetirizine hydrochloride | Tablet | `shape`: Oval Tablet<br>`primary_color`: `sage`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 20610), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Claritin** | Loratadine | Tablet | `shape`: Oval / Round Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 28889), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Allegra** | Fexofenadine hydrochloride | Tablet | `shape`: Oval Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 21231), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Flonase** | Fluticasone propionate | Liquid / Spray | `shape`: Nasal Spray Bottle<br>`primary_color`: `sage`<br>`finish`: Translucent Bottle<br>`icon`: `ic_med_liquid` | RxNorm (RxCUI: 41126), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.9 Pain Medications (Analgesics & NSAIDs)
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Advil / Motrin** | Ibuprofen | Tablet / Caplet | `shape`: Round / Caplet Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 5640), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Tylenol** | Acetaminophen (Paracetamol) | Tablet / Caplet | `shape`: Caplet / Round Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 161), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Aleve** | Naproxen sodium | Tablet | `shape`: Oval Tablet<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 7258), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Ultram** | Tramadol hydrochloride | Tablet | `shape`: Round Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 10689), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.10 Antibiotics
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Amoxil** | Amoxicillin | Capsule / Suspension | `shape`: Two-tone Capsule / Liquid<br>`primary_color`: `terracotta`<br>`accent_color`: `butter`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 723), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Zithromax (Z-Pak)** | Azithromycin | Tablet | `shape`: Oval Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 18631), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Keflex** | Cephalexin | Capsule | `shape`: Two-tone Capsule<br>`primary_color`: `sage`<br>`accent_color`: `sand`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 2231), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Cipro** | Ciprofloxacin | Tablet | `shape`: Capsule-Shaped Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 2551), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.11 Oral Contraceptives
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Alesse / Aviane** | Ethinyl estradiol / Levonorgestrel | Tablet (Dial Pack) | `shape`: Small Round Dial Pack<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 748858), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Loestrin** | Ethinyl estradiol / Norethindrone | Tablet (Dial Pack) | `shape`: Small Round Dial Pack<br>`primary_color`: `butter`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 748879), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Yaz / Yasmin** | Drospirenone / Ethinyl estradiol | Tablet (Dial Pack) | `shape`: Small Round Dial Pack<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 637119), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.12 Vitamins & Supplements
| Medication / Supplement | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Vitamin D3** | Cholecalciferol | Softgel / Tablet | `shape`: Oval Softgel<br>`primary_color`: `butter`<br>`finish`: Translucent Amber<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 2418), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Vitamin B12** | Cyanocobalamin | Tablet / Sublingual | `shape`: Round Small Tablet<br>`primary_color`: `terracotta`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 3012), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Daily Multivitamin** | Multivitamin Complex | Tablet / Softgel | `shape`: Large Oblong Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 731223), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Omega-3 Fish Oil** | Omega-3 Acid Ethyl Esters | Softgel | `shape`: Large Oval Softgel<br>`primary_color`: `butter`<br>`finish`: Translucent Amber<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 284416), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

### 4.13 Common Gastrointestinal Medications
| Medication | Generic Name | Visual Type | Visual Parameters | Source | License | Generic or Manufacturer-Specific |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Prilosec** | Omeprazole | Capsule | `shape`: Two-tone Capsule<br>`primary_color`: `sage`<br>`accent_color`: `terracotta`<br>`finish`: Dual-Tone<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 7646), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Pepcid** | Famotidine | Tablet | `shape`: Round / Square Tablet<br>`primary_color`: `sand`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 4278), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Zofran** | Ondansetron hydrochloride | Tablet | `shape`: Round / ODT Tablet<br>`primary_color`: `slate`<br>`finish`: Soft Matte<br>`icon`: `ic_med_tablet` | RxNorm (RxCUI: 7609), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |
| **Colace** | Docusate sodium | Softgel | `shape`: Oval Softgel<br>`primary_color`: `terracotta`<br>`finish`: Translucent<br>`icon`: `ic_med_capsule` | RxNorm (RxCUI: 3638), DailyMed | Public Domain (U.S. NLM / FDA) | Generic Visual Representation |

---

## 5. Implementation & Maintenance Guidelines

### Adding New Medications
1. Query the [RxNav RxNorm API](https://rxnav.nlm.nih.gov/) or DailyMed for the official Concept Unique Identifier (RxCUI) and generic name.
2. Select the visual form factor (`Capsule`, `Tablet`, `Liquid`, `Powder`, or `Injection`).
3. Assign a design system color token (`sage`, `terracotta`, `butter`, `slate`, `sand`).
4. Append the new entry to the visual catalog following the exact table structure above.
5. Update application mapping logic if preset defaults are registered in local assets/data layers.
