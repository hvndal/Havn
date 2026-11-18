#!/usr/bin/env python3
"""
One source for every translated string in Hävn for iOS.

Writes:
  ios/Havn/Localizable.xcstrings      in-app text (Xcode String Catalog)
  ios/Havn/InfoPlist.xcstrings        permission prompts and the app's display name
  ios/fastlane/metadata/<locale>/     App Store product page text, one folder per store locale

Edit the text here, run this script, then run check.py.
Every translation is marked "needs_review" until a native speaker has read it.

Run from the repo root:  python3 ios/tools/build_localization.py
"""
import json, os

ROOT = os.path.join(os.path.dirname(__file__), '..')

# In-app languages (Xcode locale codes). Spanish and French serve every region; Portuguese has two.
APP_LANGS = ['fr', 'es', 'pt-BR', 'pt-PT', 'ja', 'de', 'it', 'nb', 'sv', 'ko']

# ---------------------------------------------------------------------------------------------
# In-app strings: key (English source) -> comment, then one value per language in APP_LANGS order.
# Use Apple Health's own words for Taken / Skipped in each language; confirm them during review.
# ---------------------------------------------------------------------------------------------
S = [
 ("Today", "Tab and screen title", ["Aujourd’hui","Hoy","Hoje","Hoje","今日","Heute","Oggi","I dag","Idag","오늘"]),
 ("History", "Tab and screen title", ["Historique","Historial","Histórico","Histórico","履歴","Verlauf","Cronologia","Historikk","Historik","기록"]),
 ("Medications", "Tab and screen title", ["Médicaments","Medicamentos","Remédios","Medicamentos","お薬","Medikamente","Farmaci","Medisiner","Mediciner","약"]),
 ("Settings", "Screen title", ["Réglages","Ajustes","Ajustes","Definições","設定","Einstellungen","Impostazioni","Innstillinger","Inställningar","설정"]),
 ("Morning", "Part of the day, group header", ["Matin","Mañana","Manhã","Manhã","朝","Morgen","Mattina","Morgen","Morgon","아침"]),
 ("Afternoon", "Part of the day, group header", ["Après-midi","Tarde","Tarde","Tarde","昼","Nachmittag","Pomeriggio","Ettermiddag","Eftermiddag","오후"]),
 ("Evening", "Part of the day, group header", ["Soir","Noche","Noite","Noite","夕方","Abend","Sera","Kveld","Kväll","저녁"]),
 ("Night", "Part of the day (bedtime doses), group header", ["Nuit","Al acostarse","Hora de dormir","Hora de deitar","就寝前","Nacht","Notte","Natt","Natt","밤"]),
 ("Taken", "Button and state: the dose was taken", ["Pris","Tomado","Tomado","Tomado","服用済み","Genommen","Assunto","Tatt","Tagen","복용함"]),
 ("Skipped", "Button and state: the dose was skipped on purpose", ["Ignoré","Omitido","Pulado","Ignorado","スキップ","Übersprungen","Saltato","Hoppet over","Hoppades över","건너뜀"]),
 ("Remind Me in 10 Minutes", "Button on the dose sheet and in the notification", ["Me rappeler dans 10 minutes","Recordar en 10 minutos","Lembrar em 10 minutos","Lembrar daqui a 10 minutos","10分後に再通知","In 10 Minuten erinnern","Ricordamelo tra 10 minuti","Påminn meg om 10 minutter","Påminn mig om 10 minuter","10분 후 다시 알림"]),
 ("Log All as Taken", "Button on a group header: marks every dose in the group as taken", ["Tout marquer comme pris","Marcar todos como tomados","Marcar todos como tomados","Marcar todos como tomados","すべて服用済みにする","Alle als genommen markieren","Segna tutti come assunti","Merk alle som tatt","Markera alla som tagna","모두 복용함으로 표시"]),
 ("Undo", "Button shown for a few seconds after logging", ["Annuler","Deshacer","Desfazer","Anular","取り消す","Widerrufen","Annulla","Angre","Ångra","실행 취소"]),
 ("Add Medication", "Button and sheet title", ["Ajouter un médicament","Añadir medicamento","Adicionar remédio","Adicionar medicamento","お薬を追加","Medikament hinzufügen","Aggiungi farmaco","Legg til medisin","Lägg til medicin","약 추가"]),
 ("Next", "Button: go to the next step", ["Suivant","Siguiente","Avançar","Seguinte","次へ","Weiter","Avanti","Neste","Nästa","다음"]),
 ("Cancel", "Button", ["Annuler","Cancelar","Cancelar","Cancelar","キャンセル","Abbrechen","Annulla","Avbryt","Avbryt","취소"]),
 ("Done", "Button: close and save", ["OK","OK","OK","OK","完了","Fertig","Fine","Ferdig","Klar","완료"]),
 ("%lld of %lld taken", "Summary next to the Day Ring, e.g. 2 of 4 taken", ["%1$lld sur %2$lld pris","%1$lld de %2$lld tomados","%1$lld de %2$lld tomados","%1$lld de %2$lld tomados","%2$lld件中%1$lld件服用済み","%1$lld von %2$lld genommen","%1$lld di %2$lld assunti","%1$lld av %2$lld tatt","%1$lld av %2$lld tagna","%2$lld개 중 %1$lld개 복용함"]),
 ("Due at %@", "Under a dose that is late; %@ is a time such as 13:00", ["Prévu à %@","Toca a las %@","Previsto para %@","Previsto para as %@","%@に服用予定","Fällig um %@","Previsto alle %@","Planlagt kl. %@","Planerad kl. %@","%@ 복용 예정"]),
 ("Taken at %@", "Under a dose that was logged; %@ is a time", ["Pris à %@","Tomado a las %@","Tomado às %@","Tomado às %@","%@に服用","Genommen um %@","Assunto alle %@","Tatt kl. %@","Tagen kl. %@","%@에 복용함"]),
 ("Next: %@ at %@", "Summary line; the first %@ is a medication name, the second a time", ["Ensuite : %1$@ à %2$@","Siguiente: %1$@ a las %2$@","Próximo: %1$@ às %2$@","Seguinte: %1$@ às %2$@","次は%2$@に%1$@","Als Nächstes: %1$@ um %2$@","Prossimo: %1$@ alle %2$@","Neste: %1$@ kl. %2$@","Nästa: %1$@ kl. %2$@","다음: %2$@ %1$@"]),
 ("%@ logged", "Shown in the bar above the tab bar after logging; %@ is a medication name", ["%@ noté","%@ registrado","%@ registrado","%@ registado","%@を記録しました","%@ eingetragen","%@ registrato","%@ logget","%@ loggad","%@ 기록됨"]),
 ("Your ring is closed. Rest easy.", "Shown when every dose today is taken", ["Votre anneau est complet. Reposez-vous.","Anillo cerrado. Puedes descansar.","Seu anel está fechado. Pode descansar.","O seu anel está fechado. Pode descansar.","リングが閉じました。ゆっくり休んでください。","Dein Ring ist geschlossen. Gönn dir Ruhe.","Il tuo anello è chiuso. Riposati.","Ringen er lukket. Slapp av.","Ringen är sluten. Ta det lugnt.","링이 닫혔어요. 편히 쉬세요."]),
 ("Nothing yet", "Empty Today summary", ["Rien pour l’instant","Nada todavía","Nada ainda","Ainda nada","まだありません","Noch nichts","Ancora niente","Ingenting ennå","Inget än","아직 없음"]),
 ("Add a medication to start your ring.", "Empty Today summary, second line", ["Ajoutez un médicament pour commencer votre anneau.","Añade un medicamento para empezar tu anillo.","Adicione um remédio para começar seu anel.","Adicione um medicamento para começar o seu anel.","お薬を追加して、リングを始めましょう。","Füge ein Medikament hinzu, um deinen Ring zu starten.","Aggiungi un farmaco per iniziare il tuo anello.","Legg til en medisin for å starte ringen din.","Lägg till en medicin för att starta din ring.","약을 추가해 링을 시작하세요."]),
 ("Time for your %@ dose", "Notification title; %@ is a time such as 13:00", ["C’est l’heure de votre prise de %@","Es hora de tu toma de las %@","Hora da sua dose das %@","Hora da sua toma das %@","%@の服薬の時間です","Zeit für deine Einnahme um %@","È ora della dose delle %@","Tid for dosen kl. %@","Dags för dosen kl. %@","%@ 복용 시간입니다"]),
 ("A calm place for your medication.", "Welcome screen subtitle", ["Un endroit serein pour vos médicaments.","Un lugar tranquilo para tu medicación.","Um lugar tranquilo para seus remédios.","Um lugar tranquilo para a sua medicação.","お薬のための、穏やかな場所。","Ein ruhiger Ort für deine Medikamente.","Un posto tranquillo per i tuoi farmaci.","Et rolig sted for medisinene dine.","En lugn plats för dina mediciner.","약을 위한 차분한 공간."]),
 ("Your day, as a ring.", "Onboarding title", ["Votre journée, en anneau.","Tu día, en un anillo.","Seu dia, em um anel.","O seu dia, num anel.","一日を、リングで。","Dein Tag als Ring.","La tua giornata, in un anello.","Dagen din, som en ring.","Din dag, som en ring.","하루를 하나의 링으로."]),
 ("Stays on your iPhone.", "Onboarding title on the privacy screen", ["Tout reste sur votre iPhone.","Se queda en tu iPhone.","Fica no seu iPhone.","Fica no seu iPhone.","データはiPhoneの中だけに。","Bleibt auf deinem iPhone.","Resta sul tuo iPhone.","Blir på iPhonen din.","Stannar på din iPhone.","iPhone에만 남습니다."]),
 ("No account. No ads. Stays on your iPhone.", "Small print on the welcome screen", ["Sans compte. Sans pub. Tout reste sur votre iPhone.","Sin cuenta. Sin anuncios. Se queda en tu iPhone.","Sem conta. Sem anúncios. Fica no seu iPhone.","Sem conta. Sem anúncios. Fica no seu iPhone.","アカウントも広告も不要。データはiPhoneの中だけに。","Kein Konto. Keine Werbung. Bleibt auf deinem iPhone.","Niente account. Niente pubblicità. Resta sul tuo iPhone.","Ingen konto. Ingen reklame. Blir på iPhonen din.","Inget konto. Ingen reklam. Stannar på din iPhone.","계정도 광고도 없이, iPhone에만 남습니다."]),
 ("Dose Reminders", "Settings switch", ["Rappels de prise","Recordatorios de tomas","Lembretes de dose","Lembretes de toma","服薬リマインダー","Einnahme-Erinnerungen","Promemoria dosi","Dosepåminnelser","Dospåminnelser","복용 알림"]),
 ("Ring Through Silent Mode", "Settings switch: uses an alarm instead of a notification", ["Sonner en mode silencieux","Sonar en modo silencio","Tocar no modo silencioso","Tocar no modo silencioso","サイレントモードでも鳴らす","Auch im Stumm-Modus klingeln","Suona anche in silenzioso","Ring i stillemodus","Ring i ljudlöst läge","무음 모드에서도 울리기"]),
 ("Show Names on Lock Screen", "Settings switch; off by default", ["Afficher les noms sur l’écran verrouillé","Mostrar nombres en la pantalla bloqueada","Mostrar nomes na Tela Bloqueada","Mostrar nomes no ecrã bloqueado","ロック画面に名前を表示","Namen auf dem Sperrbildschirm zeigen","Mostra i nomi sulla schermata di blocco","Vis navn på låseskjermen","Visa namn på låsskärmen","잠금 화면에 이름 표시"]),
 ("Read Medications from Health", "Settings switch for Apple Health", ["Lire les médicaments depuis Santé","Leer medicamentos de Salud","Ler remédios do Saúde","Ler medicamentos da Saúde","ヘルスケアからお薬を読み込む","Medikamente aus Health lesen","Leggi i farmaci da Salute","Les medisiner fra Helse","Läs mediciner från Hälsa","건강 앱에서 약 읽기"]),
 ("Export", "Settings row: save a backup file", ["Exporter","Exportar","Exportar","Exportar","書き出す","Exportieren","Esporta","Eksporter","Exportera","내보내기"]),
 ("Erase All Data", "Settings row, destructive", ["Effacer toutes les données","Borrar todos los datos","Apagar todos os dados","Apagar todos os dados","すべてのデータを消去","Alle Daten löschen","Cancella tutti i dati","Slett alle data","Radera all data","모든 데이터 지우기"]),
 ("This removes every medication and dose from this iPhone. It can’t be undone.", "Confirmation under Erase All Data",
  ["Tous les médicaments et toutes les prises seront supprimés de cet iPhone. Cette action est irréversible.",
   "Se eliminarán todos los medicamentos y tomas de este iPhone. No se puede deshacer.",
   "Todos os remédios e doses serão apagados deste iPhone. Não é possível desfazer.",
   "Todos os medicamentos e tomas serão apagados deste iPhone. Não é possível anular.",
   "このiPhoneからすべてのお薬と服薬記録が削除されます。この操作は取り消せません。",
   "Alle Medikamente und Einnahmen werden von diesem iPhone gelöscht. Das kann nicht widerrufen werden.",
   "Tutti i farmaci e le dosi verranno eliminati da questo iPhone. L’operazione non può essere annullata.",
   "Alle medisiner og doser fjernes fra denne iPhonen. Dette kan ikke angres.",
   "Alla mediciner och doser tas bort från den här iPhonen. Det går inte att ångra.",
   "이 iPhone에서 모든 약과 복용 기록이 삭제됩니다. 이 작업은 실행 취소할 수 없습니다."]),
 ("Include in iPhone Backups", "Settings switch; off by default. When on, the data goes into the person’s own iPhone backup", ["Inclure dans les sauvegardes de l’iPhone","Incluir en las copias de seguridad del iPhone","Incluir nos backups do iPhone","Incluir nas cópias de segurança do iPhone","iPhoneのバックアップに含める","In iPhone-Backups einschließen","Includi nei backup dell’iPhone","Ta med i iPhone-sikkerhetskopier","Inkludera i iPhone-säkerhetskopior","iPhone 백업에 포함"]),
 ("Lock with Face ID", "Settings switch; optional app lock", ["Verrouiller avec Face ID","Bloquear con Face ID","Bloquear com Face ID","Bloquear com Face ID","Face IDでロック","Mit Face ID sperren","Blocca con Face ID","Lås med Face ID","Lås med Face ID","Face ID로 잠그기"]),
 ("About Hävn", "Settings row", ["À propos de Hävn","Acerca de Hävn","Sobre o Hävn","Acerca do Hävn","Hävnについて","Über Hävn","Informazioni su Hävn","Om Hävn","Om Hävn","Hävn 정보"]),
 ("Privacy Policy", "Settings › About Hävn row; opens the privacy policy (App Review guideline 5.1.1(i) asks for an in-app link)", ["Politique de confidentialité","Política de privacidad","Política de Privacidade","Política de privacidade","プライバシーポリシー","Datenschutzerklärung","Informativa sulla privacy","Personvernerklæring","Integritetspolicy","개인정보 처리방침"]),
 ("Hävn is a reminder tool and doesn’t give medical advice. Always follow your doctor’s or pharmacist’s instructions.",
  "Medical disclaimer (App Review guideline 1.4.1); shown in onboarding and Settings › About",
  ["Hävn est un outil de rappel et ne donne pas d’avis médical. Suivez toujours les instructions de votre médecin ou de votre pharmacien.",
   "Hävn es una herramienta de recordatorio y no ofrece consejo médico. Sigue siempre las indicaciones de tu médico o farmacéutico.",
   "O Hävn é uma ferramenta de lembrete e não oferece orientação médica. Siga sempre as instruções do seu médico ou farmacêutico.",
   "O Hävn é uma ferramenta de lembrete e não presta aconselhamento médico. Siga sempre as indicações do seu médico ou farmacêutico.",
   "Hävnはリマインダーであり、医療上の助言は行いません。必ず医師または薬剤師の指示に従ってください。",
   "Hävn ist ein Erinnerungswerkzeug und gibt keinen medizinischen Rat. Folge immer den Anweisungen deiner Ärztin, deines Arztes oder deiner Apotheke.",
   "Hävn è uno strumento di promemoria e non fornisce consigli medici. Segui sempre le indicazioni del tuo medico o farmacista.",
   "Hävn er et påminnelsesverktøy og gir ikke medisinske råd. Følg alltid instruksjonene fra legen eller apoteket.",
   "Hävn är ett påminnelseverktyg och ger inga medicinska råd. Följ alltid instruktionerna från din läkare eller ditt apotek.",
   "Hävn은 알림 도구이며 의학적 조언을 제공하지 않습니다. 항상 의사나 약사의 지시를 따르세요."]),
]

# Plural strings: key -> comment, English (one, other), then per language (one, other); one=None where the language has no plural.
P = [
 ("%lld medications are due", "Notification body; Lock Screen shows a count, not names, unless the person turns names on",
  ("%lld medication is due", "%lld medications are due"),
  [("%lld médicament à prendre", "%lld médicaments à prendre"),
   ("%lld medicamento pendiente", "%lld medicamentos pendientes"),
   ("%lld remédio para tomar", "%lld remédios para tomar"),
   ("%lld medicamento para tomar", "%lld medicamentos para tomar"),
   (None, "%lld件のお薬の時間です"),
   ("%lld Medikament fällig", "%lld Medikamente fällig"),
   ("%lld farmaco da assumere", "%lld farmaci da assumere"),
   ("%lld medisin skal tas", "%lld medisiner skal tas"),
   ("%lld medicin ska tas", "%lld mediciner ska tas"),
   (None, "복용할 약 %lld개")]),
]

# Info.plist strings (permission prompts). Key names: confirm NSAlarmKitUsageDescription in Xcode when adding AlarmKit.
IP = [
 ("CFBundleDisplayName", "App name under the icon", ["Hävn"]*10),
 ("NSHealthShareUsageDescription", "Shown when Hävn asks to read medications from Apple Health",
  ["Hävn lit votre liste de médicaments dans Santé pour vous éviter de la saisir. Rien n’est écrit dans Santé ni envoyé ailleurs.",
   "Hävn lee tu lista de medicamentos de Salud para que no tengas que escribirla. No escribe nada en Salud ni envía tus datos a ningún sitio.",
   "O Hävn lê sua lista de remédios do app Saúde para você não precisar digitá-la. Ele não grava nada no Saúde nem envia seus dados para lugar nenhum.",
   "O Hävn lê a sua lista de medicamentos da app Saúde para não ter de a escrever. Não grava nada na Saúde nem envia os seus dados para lado nenhum.",
   "お薬のリストを入力しなくて済むよう、Hävnはヘルスケアからリストを読み込みます。ヘルスケアへの書き込みや、データの送信は行いません。",
   "Hävn liest deine Medikamentenliste aus Health, damit du sie nicht abtippen musst. Es schreibt nichts in Health und sendet deine Daten nirgendwohin.",
   "Hävn legge l’elenco dei farmaci da Salute così non devi digitarlo. Non scrive nulla in Salute e non invia i tuoi dati da nessuna parte.",
   "Hävn leser medisinlisten din fra Helse, så du slipper å skrive den inn. Appen skriver ingenting til Helse og sender ikke dataene dine noe sted.",
   "Hävn läser din medicinlista från Hälsa så att du slipper skriva in den. Appen skriver ingenting till Hälsa och skickar inte dina data någonstans.",
   "약 목록을 직접 입력하지 않도록 Hävn이 건강 앱에서 목록을 읽어 옵니다. 건강 앱에 기록하거나 데이터를 외부로 보내지 않습니다."]),
 ("NSAlarmKitUsageDescription", "Shown when Hävn asks to schedule alarms (AlarmKit)",
  ["Permet à Hävn de faire sonner une alarme pour les médicaments que vous choisissez, même lorsque votre iPhone est en mode silencieux.",
   "Permite que Hävn haga sonar una alarma para los medicamentos que elijas, incluso con el iPhone en silencio.",
   "Permite que o Hävn toque um alarme para os remédios que você escolher, mesmo com o iPhone no silencioso.",
   "Permite que o Hävn toque um alarme para os medicamentos que escolher, mesmo com o iPhone em silêncio.",
   "選んだお薬について、iPhoneがサイレントモードでもHävnがアラームを鳴らせるようにします。",
   "Damit Hävn für ausgewählte Medikamente einen Wecker klingeln lassen kann, auch wenn dein iPhone stumm ist.",
   "Permette a Hävn di far suonare una sveglia per i farmaci che scegli, anche quando l’iPhone è in modalità silenziosa.",
   "Lar Hävn ringe en alarm for medisinene du velger, også når iPhonen er i stillemodus.",
   "Låter Hävn ringa ett alarm för de mediciner du väljer, även när din iPhone är i ljudlöst läge.",
   "선택한 약에 대해 iPhone이 무음 모드여도 Hävn이 알람을 울릴 수 있게 합니다."]),
 ("NSFaceIDUsageDescription", "Shown if the person turns on the optional Face ID lock",
  ["Utilisez Face ID pour déverrouiller Hävn.","Usa Face ID para desbloquear Hävn.","Use o Face ID para desbloquear o Hävn.","Use o Face ID para desbloquear o Hävn.",
   "Face IDでHävnのロックを解除します。","Mit Face ID Hävn entsperren.","Usa Face ID per sbloccare Hävn.","Bruk Face ID for å låse opp Hävn.","Använd Face ID för att låsa upp Hävn.","Face ID로 Hävn 잠금을 해제합니다."]),
]
IP_EN = {
 "CFBundleDisplayName": "Hävn",
 "NSHealthShareUsageDescription": "Hävn reads your medication list from Health so you don’t have to type it in. It never writes to Health or sends your data anywhere.",
 "NSAlarmKitUsageDescription": "Lets Hävn ring an alarm for the medications you choose, even when your iPhone is on Silent.",
 "NSFaceIDUsageDescription": "Use Face ID to unlock Hävn.",
}

# ---------------------------------------------------------------------------------------------
# App Store product pages. Store locale codes as fastlane uses them (confirm when running `deliver`).
# Fields: name (30), subtitle (30), promotional_text (170), keywords (100 bytes, commas, no spaces), description (4000).
# Words already in the name or subtitle are left out of keywords: Apple indexes all three together.
# ---------------------------------------------------------------------------------------------
DESC_EN = """Hävn is a calm, private medication reminder for iPhone.

YOUR DAY AS A RING
Each dose today is one piece of the Day Ring. Log it and it fills in with your medication’s {color}. By tonight, the ring closes.

BUILT FOR REAL ROUTINES
• Doses grouped by morning, afternoon, evening and night
• Log a whole group at once with Log All as Taken
• Reminders you can answer from the notification: Taken, Skipped or Remind Me in 10 Minutes
• An optional alarm that rings even on Silent, for doses that can’t wait
• Pills that look like yours: pick the shape and {colors}
• Supply tracking with a refill reminder
• History by week and month, with a calm summary
• Several people on one iPhone, each with their own medications
• Widgets for the Home Screen and Lock Screen
• Import your medication list from Apple Health

PRIVATE BY DESIGN
No account, no ads, no analytics and no tracking. Hävn has no servers: your medications stay on your iPhone. Export a backup file whenever you like.

FREE
Hävn is free, with no in-app purchases.

Hävn is a reminder tool and doesn’t give medical advice. Always follow the instructions from your doctor or pharmacist, and check with them before changing how you take a medication."""

L = {}
L['en-US'] = dict(name="Hävn: Medication Reminder", subtitle="Private, offline pill tracker",
  promotional_text="Your day as a ring. Log a dose in one tap and close your ring by tonight. No account, no ads, no tracking: everything stays on your iPhone.",
  keywords="medicine,meds,dose,vitamin,supplement,alarm,schedule,refill,health,log,diary,prescription,tablet",
  description=DESC_EN.format(color="color", colors="colors"))
L['en-GB'] = dict(L['en-US'], description=DESC_EN.format(color="colour", colors="colours"))
L['fr-FR'] = dict(name="Hävn : rappel de médicaments", subtitle="Pilulier privé, hors ligne",
  promotional_text="Votre journée en anneau. Notez une prise d’un geste et refermez l’anneau d’ici ce soir. Sans compte, sans pub, sans pistage : tout reste sur votre iPhone.",
  keywords="pilule,comprimé,traitement,prise,dose,vitamine,complément,alarme,ordonnance,santé,oubli,journal",
  description="""Hävn est un rappel de médicaments calme et privé pour iPhone.

VOTRE JOURNÉE EN ANNEAU
Chaque prise du jour est un segment de l’anneau. Notez-la et elle se remplit de la couleur de votre médicament. Le soir venu, l’anneau se referme.

PENSÉ POUR LA VRAIE VIE
• Prises regroupées par matin, après-midi, soir et nuit
• Tout noter d’un coup avec « Tout marquer comme pris »
• Rappels auxquels répondre depuis la notification : Pris, Ignoré ou Me rappeler dans 10 minutes
• Une alarme facultative qui sonne même en mode silencieux, pour les prises qui ne peuvent pas attendre
• Des comprimés qui ressemblent aux vôtres : choisissez la forme et les couleurs
• Suivi du stock avec rappel de renouvellement
• Historique par semaine et par mois, avec un résumé serein
• Plusieurs personnes sur un même iPhone, chacune avec ses médicaments
• Widgets pour l’écran d’accueil et l’écran verrouillé
• Importez votre liste de médicaments depuis l’app Santé

PRIVÉ PAR CONCEPTION
Sans compte, sans publicité, sans statistiques ni pistage. Hävn n’a pas de serveur : vos médicaments restent sur votre iPhone. Exportez une sauvegarde quand vous le souhaitez.

GRATUIT
Hävn est gratuit, sans achat intégré.

Hävn est un outil de rappel et ne donne pas d’avis médical. Suivez toujours les instructions de votre médecin ou de votre pharmacien, et demandez-leur conseil avant de modifier la façon dont vous prenez un médicament.""")
L['fr-CA'] = dict(L['fr-FR'])
L['es-ES'] = dict(name="Hävn: aviso de medicamentos", subtitle="Pastillero privado y local",
  promotional_text="Tu día en un anillo. Registra una toma con un toque y cierra el anillo antes de la noche. Sin cuenta, sin anuncios, sin rastreo: todo se queda en tu iPhone.",
  keywords="pastilla,medicina,recordatorio,dosis,toma,vitamina,suplemento,alarma,tratamiento,receta,salud",
  description="""Hävn es un recordatorio de medicación tranquilo y privado para iPhone.

TU DÍA EN UN ANILLO
Cada toma del día es una parte del anillo. Regístrala y se llena con el color de tu medicamento. Por la noche, el anillo se cierra.

PENSADO PARA TU RUTINA
• Tomas agrupadas por mañana, tarde, noche y al acostarse
• Registra un grupo entero con «Marcar todos como tomados»
• Recordatorios que respondes desde la notificación: Tomado, Omitido o Recordar en 10 minutos
• Una alarma opcional que suena incluso en silencio, para las tomas que no pueden esperar
• Pastillas que se parecen a las tuyas: elige la forma y los colores
• Control de existencias con aviso para reponer
• Historial por semana y por mes, con un resumen tranquilo
• Varias personas en un mismo iPhone, cada una con sus medicamentos
• Widgets para la pantalla de inicio y la pantalla bloqueada
• Importa tu lista de medicamentos desde la app Salud

PRIVADO DESDE EL DISEÑO
Sin cuenta, sin anuncios, sin analíticas y sin rastreo. Hävn no tiene servidores: tus medicamentos se quedan en tu iPhone. Exporta una copia de seguridad cuando quieras.

GRATIS
Hävn es gratis y no tiene compras dentro de la app.

Hävn es una herramienta de recordatorio y no ofrece consejo médico. Sigue siempre las indicaciones de tu médico o farmacéutico y consúltales antes de cambiar la forma en que tomas un medicamento.""")
L['es-MX'] = dict(L['es-ES'])
L['pt-BR'] = dict(name="Hävn: lembrete de remédios", subtitle="Controle privado de remédios",
  promotional_text="Seu dia em um anel. Registre uma dose com um toque e feche o anel até a noite. Sem conta, sem anúncios, sem rastreamento: tudo fica no seu iPhone.",
  keywords="pílula,comprimido,medicamento,dose,vitamina,suplemento,alarme,tratamento,receita,saúde,horário",
  description="""O Hävn é um lembrete de remédios calmo e privado para iPhone.

SEU DIA EM UM ANEL
Cada dose do dia é uma parte do anel. Registre e ela ganha a cor do seu remédio. À noite, o anel se fecha.

FEITO PARA A SUA ROTINA
• Doses agrupadas por manhã, tarde, noite e hora de dormir
• Registre um grupo inteiro com "Marcar todos como tomados"
• Lembretes que você responde pela notificação: Tomado, Pulado ou Lembrar em 10 minutos
• Um alarme opcional que toca mesmo no silencioso, para doses que não podem esperar
• Comprimidos parecidos com os seus: escolha o formato e as cores
• Controle de estoque com aviso para repor
• Histórico por semana e por mês, com um resumo tranquilo
• Várias pessoas no mesmo iPhone, cada uma com seus remédios
• Widgets para a Tela de Início e a Tela Bloqueada
• Importe sua lista de remédios do app Saúde

PRIVADO DESDE O INÍCIO
Sem conta, sem anúncios, sem análises e sem rastreamento. O Hävn não tem servidores: seus remédios ficam no seu iPhone. Exporte um backup quando quiser.

GRÁTIS
O Hävn é grátis e não tem compras dentro do app.

O Hävn é uma ferramenta de lembrete e não oferece orientação médica. Siga sempre as instruções do seu médico ou farmacêutico e fale com eles antes de mudar a forma como você toma um remédio.""")
L['pt-PT'] = dict(name="Hävn: lembrete de medicação", subtitle="Controlo privado de medicação",
  promotional_text="O seu dia num anel. Registe uma toma com um toque e feche o anel até à noite. Sem conta, sem anúncios, sem rastreio: tudo fica no seu iPhone.",
  keywords="comprimido,medicamento,toma,dose,vitamina,suplemento,alarme,tratamento,receita,saúde,horário",
  description="""O Hävn é um lembrete de medicação calmo e privado para iPhone.

O SEU DIA NUM ANEL
Cada toma do dia é uma parte do anel. Registe-a e ela ganha a cor do seu medicamento. À noite, o anel fecha-se.

FEITO PARA A SUA ROTINA
• Tomas agrupadas por manhã, tarde, noite e hora de deitar
• Registe um grupo inteiro com "Marcar todos como tomados"
• Lembretes a que responde a partir da notificação: Tomado, Ignorado ou Lembrar daqui a 10 minutos
• Um alarme opcional que toca mesmo em silêncio, para tomas que não podem esperar
• Comprimidos parecidos com os seus: escolha a forma e as cores
• Controlo de stock com aviso para repor
• Histórico por semana e por mês, com um resumo tranquilo
• Várias pessoas no mesmo iPhone, cada uma com os seus medicamentos
• Widgets para o ecrã principal e o ecrã bloqueado
• Importe a sua lista de medicamentos da app Saúde

PRIVADO DE RAIZ
Sem conta, sem anúncios, sem estatísticas e sem rastreio. O Hävn não tem servidores: os seus medicamentos ficam no seu iPhone. Exporte uma cópia de segurança quando quiser.

GRATUITO
O Hävn é gratuito e não tem compras integradas.

O Hävn é uma ferramenta de lembrete e não presta aconselhamento médico. Siga sempre as indicações do seu médico ou farmacêutico e fale com eles antes de alterar a forma como toma um medicamento.""")
L['de-DE'] = dict(name="Hävn: Medikamenten-Erinnerung", subtitle="Privater Tabletten-Tracker",
  promotional_text="Dein Tag als Ring. Trag eine Einnahme mit einem Tippen ein und schließ den Ring bis zum Abend. Kein Konto, keine Werbung, kein Tracking: alles bleibt auf deinem iPhone.",
  keywords="Pille,Medizin,Arznei,Dosis,Einnahme,Vitamin,Wecker,Rezept,Gesundheit,Tagebuch,Nahrungsergänzung",
  description="""Hävn ist eine ruhige, private Medikamenten-Erinnerung für das iPhone.

DEIN TAG ALS RING
Jede Einnahme des Tages ist ein Stück des Tagesrings. Trag sie ein, und sie füllt sich in der Farbe deines Medikaments. Am Abend schließt sich der Ring.

FÜR DEN ECHTEN ALLTAG
• Einnahmen nach Morgen, Nachmittag, Abend und Nacht geordnet
• Eine ganze Gruppe auf einmal mit „Alle als genommen markieren“
• Erinnerungen, die du direkt in der Mitteilung beantwortest: Genommen, Übersprungen oder In 10 Minuten erinnern
• Ein optionaler Wecker, der auch im Stumm-Modus klingelt, für Einnahmen, die nicht warten können
• Tabletten, die wie deine aussehen: Form und Farben wählen
• Vorrat im Blick, mit Erinnerung zum Nachkaufen
• Verlauf nach Woche und Monat, mit einer ruhigen Zusammenfassung
• Mehrere Personen auf einem iPhone, jede mit eigenen Medikamenten
• Widgets für Home-Bildschirm und Sperrbildschirm
• Medikamentenliste aus der Health-App importieren

PRIVAT VON ANFANG AN
Kein Konto, keine Werbung, keine Analyse, kein Tracking. Hävn hat keine Server: Deine Medikamente bleiben auf deinem iPhone. Exportiere eine Sicherung, wann immer du willst.

KOSTENLOS
Hävn ist kostenlos, ohne In-App-Käufe.

Hävn ist ein Erinnerungswerkzeug und gibt keinen medizinischen Rat. Folge immer den Anweisungen deiner Ärztin, deines Arztes oder deiner Apotheke und sprich mit ihnen, bevor du die Einnahme eines Medikaments änderst.""")
L['it'] = dict(name="Hävn: promemoria farmaci", subtitle="Diario delle pillole, privato",
  promotional_text="La tua giornata in un anello. Registra una dose con un tocco e chiudi l’anello entro sera. Niente account, pubblicità o tracciamento: tutto resta sul tuo iPhone.",
  keywords="medicina,medicinale,compressa,dose,vitamina,integratore,sveglia,terapia,ricetta,salute,orario",
  description="""Hävn è un promemoria per i farmaci, calmo e privato, per iPhone.

LA TUA GIORNATA IN UN ANELLO
Ogni dose del giorno è una parte dell’anello. Registrala e si riempie del colore del tuo farmaco. In serata, l’anello si chiude.

PENSATO PER LA VITA REALE
• Dosi raggruppate per mattina, pomeriggio, sera e notte
• Registra un intero gruppo con “Segna tutti come assunti”
• Promemoria a cui rispondi dalla notifica: Assunto, Saltato o Ricordamelo tra 10 minuti
• Una sveglia facoltativa che suona anche in modalità silenziosa, per le dosi che non possono aspettare
• Pillole che somigliano alle tue: scegli la forma e i colori
• Controllo delle scorte con promemoria per il riacquisto
• Cronologia per settimana e mese, con un riepilogo sereno
• Più persone sullo stesso iPhone, ognuna con i propri farmaci
• Widget per la schermata Home e la schermata di blocco
• Importa l’elenco dei farmaci dall’app Salute

PRIVATO PER SCELTA
Nessun account, nessuna pubblicità, nessuna analisi e nessun tracciamento. Hävn non ha server: i tuoi farmaci restano sul tuo iPhone. Esporta un backup quando vuoi.

GRATIS
Hävn è gratuito e non ha acquisti in-app.

Hävn è uno strumento di promemoria e non fornisce consigli medici. Segui sempre le indicazioni del tuo medico o farmacista e consultali prima di cambiare il modo in cui assumi un farmaco.""")
L['no'] = dict(name="Hävn: Medisinpåminnelse", subtitle="Privat pillelogg, uten konto",
  promotional_text="Dagen din som en ring. Logg en dose med ett trykk og lukk ringen innen kvelden. Ingen konto, ingen reklame, ingen sporing: alt blir på iPhonen din.",
  keywords="medisin,tablett,pille,dose,vitamin,kosttilskudd,alarm,resept,helse,legemiddel,timeplan",
  description="""Hävn er en rolig og privat medisinpåminnelse for iPhone.

DAGEN DIN SOM EN RING
Hver dose i dag er én del av dagsringen. Logg den, så fylles den med fargen til medisinen din. Innen kvelden lukkes ringen.

LAGET FOR EKTE HVERDAGER
• Doser gruppert etter morgen, ettermiddag, kveld og natt
• Logg en hel gruppe på én gang med «Merk alle som tatt»
• Påminnelser du kan svare på rett fra varslingen: Tatt, Hoppet over eller Påminn meg om 10 minutter
• En valgfri alarm som ringer selv i stillemodus, for doser som ikke kan vente
• Piller som ligner på dine: velg form og farger
• Oversikt over beholdning med påminnelse om påfyll
• Historikk per uke og måned, med en rolig oppsummering
• Flere personer på samme iPhone, hver med sine egne medisiner
• Widgeter for Hjem-skjermen og låseskjermen
• Importer medisinlisten din fra Helse-appen

PRIVAT FRA BUNNEN AV
Ingen konto, ingen reklame, ingen analyse og ingen sporing. Hävn har ingen servere: medisinene dine blir på iPhonen din. Eksporter en sikkerhetskopi når du vil.

GRATIS
Hävn er gratis, uten kjøp i appen.

Hävn er et påminnelsesverktøy og gir ikke medisinske råd. Følg alltid instruksjonene fra legen eller apoteket, og snakk med dem før du endrer hvordan du tar en medisin.""")
L['sv'] = dict(name="Hävn: Medicinpåminnelse", subtitle="Privat pillerlogg, utan konto",
  promotional_text="Din dag som en ring. Logga en dos med ett tryck och slut ringen innan kvällen. Inget konto, ingen reklam, ingen spårning: allt stannar på din iPhone.",
  keywords="medicin,tablett,piller,dos,vitamin,kosttillskott,alarm,recept,hälsa,läkemedel,schema",
  description="""Hävn är en lugn och privat medicinpåminnelse för iPhone.

DIN DAG SOM EN RING
Varje dos i dag är en del av dagsringen. Logga den så fylls den med färgen på din medicin. Till kvällen sluts ringen.

GJORD FÖR RIKTIGA VARDAGAR
• Doser grupperade efter morgon, eftermiddag, kväll och natt
• Logga en hel grupp på en gång med ”Markera alla som tagna”
• Påminnelser som du svarar på direkt i notisen: Tagen, Hoppades över eller Påminn mig om 10 minuter
• Ett valfritt alarm som ringer även i ljudlöst läge, för doser som inte kan vänta
• Piller som liknar dina: välj form och färger
• Koll på förrådet med påminnelse om påfyllning
• Historik per vecka och månad, med en lugn sammanfattning
• Flera personer på samma iPhone, var och en med sina egna mediciner
• Widgetar för hemskärmen och låsskärmen
• Importera din medicinlista från appen Hälsa

PRIVAT IN I MINSTA DETALJ
Inget konto, ingen reklam, ingen analys och ingen spårning. Hävn har inga servrar: dina mediciner stannar på din iPhone. Exportera en säkerhetskopia när du vill.

GRATIS
Hävn är gratis, utan köp i appen.

Hävn är ett påminnelseverktyg och ger inga medicinska råd. Följ alltid instruktionerna från din läkare eller ditt apotek och prata med dem innan du ändrar hur du tar ett läkemedel.""")
L['ja'] = dict(name="Hävn：服薬リマインダー", subtitle="広告なし、アカウント不要の服薬管理",
  promotional_text="一日をリングで。ワンタップで服薬を記録して、夜までにリングを閉じましょう。アカウントも広告もトラッキングもなし。すべてがあなたのiPhoneの中に。",
  keywords="薬,お薬,飲み忘れ,錠剤,サプリ,ビタミン,アラーム,健康,お薬手帳,処方",
  description="""Hävnは、iPhoneのための穏やかでプライベートな服薬リマインダーです。

一日をリングで
今日の服薬は、ひとつひとつがデイリングの一部。記録すると、そのお薬の色でリングが埋まります。夜にはリングが閉じます。

毎日の暮らしに合わせて
・朝・昼・夕方・就寝前ごとに服薬をまとめて表示
・「すべて服用済みにする」でまとめて記録
・通知から直接「服用済み」「スキップ」「10分後に再通知」
・サイレントモードでも鳴るアラーム（任意）。遅らせられない服薬に
・あなたのお薬に似た見た目：形と色を選べます
・残量の管理と補充のお知らせ
・週ごと・月ごとの履歴と、穏やかなまとめ
・1台のiPhoneで複数の人が、それぞれのお薬を管理
・ホーム画面とロック画面のウィジェット
・ヘルスケアAppからお薬のリストを読み込み

最初からプライベート
アカウントなし、広告なし、分析なし、トラッキングなし。Hävnにはサーバーがありません。お薬の情報はあなたのiPhoneの中にとどまります。バックアップはいつでもファイルに書き出せます。

無料
Hävnは無料で、App内課金もありません。

Hävnはリマインダーであり、医療上の助言は行いません。必ず医師または薬剤師の指示に従い、服用方法を変える前には相談してください。""")
L['ko'] = dict(name="Hävn: 복약 알림", subtitle="계정 없이 쓰는 나만의 복약 관리",
  promotional_text="하루를 하나의 링으로. 한 번의 탭으로 복용을 기록하고 저녁까지 링을 닫아 보세요. 계정, 광고, 추적 없이 모든 정보가 iPhone에만 남습니다.",
  keywords="약,알약,복용,영양제,비타민,알람,건강,약먹기,처방,약통",
  description="""Hävn은 iPhone을 위한 차분하고 프라이버시를 지키는 복약 알림 앱입니다.

하루를 하나의 링으로
오늘의 복용 하나하나가 데이 링의 한 조각입니다. 기록하면 그 약의 색으로 채워지고, 저녁이 되면 링이 닫힙니다.

실제 생활에 맞춘 기능
• 아침, 오후, 저녁, 밤으로 나눈 복용 일정
• '모두 복용함으로 표시'로 한 번에 기록
• 알림에서 바로 응답: 복용함, 건너뜀, 10분 후 다시 알림
• 무음 모드에서도 울리는 선택형 알람. 미룰 수 없는 약을 위해
• 내 약과 닮은 모양: 모양과 색상을 선택
• 남은 양 관리와 재구매 알림
• 주별·월별 기록과 차분한 요약
• 한 대의 iPhone에서 여러 사람이 각자의 약을 관리
• 홈 화면과 잠금 화면 위젯
• 건강 앱에서 약 목록 가져오기

처음부터 프라이버시 중심
계정, 광고, 분석, 추적이 없습니다. Hävn에는 서버가 없어서 약 정보는 당신의 iPhone에만 남습니다. 원할 때 언제든 백업 파일로 내보낼 수 있습니다.

무료
Hävn은 무료이며 앱 내 구입이 없습니다.

Hävn은 알림 도구이며 의학적 조언을 제공하지 않습니다. 항상 의사나 약사의 지시를 따르고, 복용 방법을 바꾸기 전에는 상담하세요.""")

# Same URLs on every product page. Both must be live before submission (App Review 5.1.1(i)).
PRIVACY_URL = "https://mander.tech/havn/privacy"
SUPPORT_URL = "https://mander.tech/havn"


def unit(v, state='needs_review'):
    return {"stringUnit": {"state": state, "value": v}}

def build_catalog(rows, plural_rows=(), en_values=None):
    out = {"sourceLanguage": "en", "strings": {}, "version": "1.0"}
    for key, comment, vals in rows:
        loc = {lang: unit(v) for lang, v in zip(APP_LANGS, vals)}
        if en_values and key in en_values:
            loc = {"en": unit(en_values[key], 'translated'), **loc}
        out["strings"][key] = {"comment": comment, "extractionState": "manual", "localizations": loc}
    for key, comment, (en_one, en_other), per in plural_rows:
        loc = {"en": {"variations": {"plural": {"one": unit(en_one, 'translated'), "other": unit(en_other, 'translated')}}}}
        for lang, (one, other) in zip(APP_LANGS, per):
            forms = {"other": unit(other)}
            if one is not None: forms = {"one": unit(one), **forms}
            loc[lang] = {"variations": {"plural": forms}}
        out["strings"][key] = {"comment": comment, "extractionState": "manual", "localizations": loc}
    return out

def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f: f.write(text)

if __name__ == '__main__':
    write(os.path.join(ROOT, 'Havn', 'Localizable.xcstrings'), json.dumps(build_catalog(S, P), ensure_ascii=False, indent=2) + '\n')
    write(os.path.join(ROOT, 'Havn', 'InfoPlist.xcstrings'), json.dumps(build_catalog(IP, en_values=IP_EN), ensure_ascii=False, indent=2) + '\n')
    for loc, fields in L.items():
        d = os.path.join(ROOT, 'fastlane', 'metadata', loc)
        for k, v in fields.items(): write(os.path.join(d, k + '.txt'), v + '\n')
        write(os.path.join(d, 'privacy_url.txt'), PRIVACY_URL + '\n')
        write(os.path.join(d, 'support_url.txt'), SUPPORT_URL + '\n')
    print(f"wrote {len(S)+len(P)} in-app strings x {len(APP_LANGS)} languages, {len(IP)} Info.plist strings, {len(L)} App Store locales")
