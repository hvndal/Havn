import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { HavnBrandLogo } from '../components/HavnBrandLogo';
import { HavnButton } from '../components/HavnButton';
import { HavnAmbientField, HavnBreathingMark } from '../components/HavnAmbientField';
import { AVATAR_COLORS } from '../theme/tokens';
import { soundManager } from '../audio/soundManager';

interface OnboardingScreenProps {
  onComplete: (name: string, age: number, color: string) => void;
}

export const OnboardingScreen: React.FC<OnboardingScreenProps> = ({ onComplete }) => {
  const [step, setStep] = useState<1 | 2>(1);
  const [name, setName] = useState('');
  const [age, setAge] = useState('');
  const [selectedColor, setSelectedColor] = useState(AVATAR_COLORS[0]);
  const [nameError, setNameError] = useState(false);

  const handleNext = () => {
    if (!name.trim()) {
      setNameError(true);
      return;
    }
    soundManager.playCeramicClick();
    setStep(2);
  };

  const handleFinish = () => {
    soundManager.playSoftChime();
    onComplete(name.trim(), parseInt(age, 10) || 0, selectedColor);
  };

  return (
    <div className="relative min-h-screen w-full flex items-center justify-center p-6 bg-[#FBFBFA] dark:bg-[#141613] text-[#141613] dark:text-[#EDEDEA] overflow-hidden">
      <HavnAmbientField />

      <div className="relative z-10 w-full max-w-md p-8 rounded-3xl bg-[#FFFFFF]/90 dark:bg-[#1C1F1A]/90 backdrop-blur-md border border-[#E2E1D9] dark:border-[#292E26] shadow-xl">
        <div className="flex flex-col items-center text-center mb-8">
          <HavnBrandLogo iconSize={52} showWordmark={true} showTagline={true} />
          <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-3 max-w-xs leading-relaxed">
            A quiet sanctuary for your daily health routine. Track medications in calm 3D space.
          </p>
        </div>

        <AnimatePresence mode="wait">
          {step === 1 ? (
            <motion.div
              key="step1"
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 10 }}
              className="space-y-4"
            >
              <div>
                <label className="text-[11px] font-bold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase block mb-1.5">
                  Your Name
                </label>
                <input
                  type="text"
                  placeholder="e.g. Herman, Maya"
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value);
                    if (nameError) setNameError(false);
                  }}
                  className={`w-full px-4 py-3 rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-[#141613] dark:text-[#EDEDEA] placeholder-[#8C9287] border focus:border-[#516351] dark:focus:border-[#7B947B] focus:outline-hidden ${
                    nameError ? 'border-[#9A5637]' : 'border-transparent'
                  }`}
                />
                {nameError && (
                  <span className="text-xs text-[#9A5637] mt-1 block">
                    Please provide your name to personalize your shelf.
                  </span>
                )}
              </div>

              <div>
                <label className="text-[11px] font-bold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase block mb-1.5">
                  Age (optional)
                </label>
                <input
                  type="number"
                  placeholder="e.g. 32"
                  value={age}
                  onChange={(e) => setAge(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-[#141613] dark:text-[#EDEDEA] placeholder-[#8C9287] border border-transparent focus:border-[#516351] dark:focus:border-[#7B947B] focus:outline-hidden"
                />
              </div>

              <div className="pt-4">
                <HavnButton text="Continue" size="large" onClick={handleNext} />
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="step2"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              className="space-y-6 text-center"
            >
              <div>
                <span className="text-xs font-semibold text-[#8C9287] uppercase tracking-wider">
                  Personalize
                </span>
                <h3 className="text-lg font-bold text-[#141613] dark:text-[#EDEDEA] mt-1">
                  Choose your avatar color
                </h3>
              </div>

              {/* Avatar Preview */}
              <div className="flex justify-center">
                <div
                  style={{ backgroundColor: selectedColor }}
                  className="w-20 h-20 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-md transition-all duration-300"
                >
                  {name[0]?.toUpperCase() || 'H'}
                </div>
              </div>

              <div className="flex items-center justify-center gap-3">
                {AVATAR_COLORS.map((c) => (
                  <button
                    key={c}
                    onClick={() => {
                      soundManager.playSoftTap();
                      setSelectedColor(c);
                    }}
                    style={{ backgroundColor: c }}
                    className={`w-9 h-9 rounded-full transition-transform cursor-pointer ${
                      selectedColor === c ? 'scale-125 ring-4 ring-[#516351]/30' : 'hover:scale-110'
                    }`}
                  />
                ))}
              </div>

              <div className="pt-2 flex items-center gap-3">
                <HavnButton
                  text="Back"
                  tone="ghost"
                  size="large"
                  onClick={() => setStep(1)}
                  className="flex-1"
                />
                <HavnButton
                  text="Begin Hävn"
                  tone="primary"
                  size="large"
                  onClick={handleFinish}
                  className="flex-2"
                />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};
