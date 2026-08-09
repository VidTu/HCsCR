<img alt src=https://github.com/VidTu/HCsCR/raw/main/docs/hcscr.png>

# HCsCR

Удаляй свои кристаллы края, прежде чем сервер даже узнает, что ты ударил их!

## Язык (Language)

- [English](https://github.com/VidTu/HCsCR/blob/main/docs/README.md)
- **Русский**

## Скачать

- [GitHub Releases](https://github.com/VidTu/HCsCR/releases)

## Зависимости

- Fabric, Forge, NeoForge или Quilt
- Minecraft ([Поддерживаемые версии](#версии))
- **Только для Fabric/Quilt**: [Fabric API](https://modrinth.com/mod/fabric-api)
  или [QFAPI/QSL](https://modrinth.com/mod/qsl) (*Обязательно*)
- **Только для Fabric/Quilt**: [Mod Menu](https://modrinth.com/mod/modmenu)
  (*Необязательно*)

## О проекте

PvP на кристаллах ("кпвп" или "cpvp") стало достаточно популярным в Minecraft,
но сами кристаллы никогда не предназначались для боёвки. Из-за этого задержка
(пинг) играет значительную роль в кристалльном бою, влияя на скорость спама.
Этот мод помогает уменьшить (но НЕ убрать полностью) эффекты пинга из
кристалльных сражений. Такие моды как этот обычно называются *клиентским
кристалл-оптимайзером*. Также этот мод убирает эффекты пинга для боёв на якорях,
но пинг там не сильно влияет на ход сражения и может даже изредка помогать. Этот
мод - не единственный кристалл-оптимайзер, но он самый настраиваемый из всех.

*ВНИМАНИЕ*: Серверный кристалл-оптимайзер в виде плагина (т.е. не этот мод)
гораздо более эффективен. Проверьте команду `/fastcrystals` или схожие команды
на вашем любимом сервере. Попросите администрацию установить один из таких
серверных оптимайзеров, если вы не нашли этой или схожей команды.

## Версии

| Поддержка                 | Версии                                                                                                          | Заметка                                                                                                                                         |
|---------------------------|-----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Бета&nbsp;&#x1F195;       | 26.3                                                                                                            | Версии со свежей поддержкой. Могут быть нестабильны, но тут ведётся *активная* разработка функционала и *активное* исправление багов.           |
| Активная&nbsp;&#x2705;    | 26.2, 26.1.2, 1.21.11, 1.21.1                                                                                   | Активно поддерживаемые версии. Хорошо протестированы, функционал *часто* портируется с новых версий, ведётся *активное* исправление багов.      |
| Архаичная&nbsp;&#x2753;   | 1.21.10, 1.21.8, 1.21.5, 1.21.4, 1.21.3, 1.20.6, 1.20.4, 1.20.2, 1.20.1, 1.19.4, 1.19.2, 1.18.2, 1.17.1, 1.16.5 | Версии, поддерживаемые по возможности. Функционал *иногда* портируется. Баги *часто* исправляются. Критические баги будут *активно* исправлены. |
| Закончилась&nbsp;&#x274C; | 1.21.9, 1.21.7, 1.21.6, 1.21.2, 1.21, 1.20.5, 1.20.3, 1.20, 1.19.3, 1.19.1                                      | Нет поддержки. Устарели. **Используйте на свой риск.**                                                                                          |

Другие версии никогда не поддерживались.

## ЧаВО (FAQ)

**В**: Мне нужна помощь, у меня есть вопросы, я хочу связаться с разработчиком.  
**А**: Можете зайти на [сервер Discord](https://discord.gg/Q6saSVSuYQ).
(главный разработчик говорит по-русски)

**В**: Где я могу скачать этот мод?  
**А**: На [GitHub Releases](https://github.com/VidTu/HCsCR/releases).
Нестабильные версии можно скачать на
[GitHub Actions](https://github.com/VidTu/HCsCR/actions).
Для них потребуется аккаунт GitHub.

**В**: Какие загрузчики модов поддерживаются?  
**А**: Поддерживаются Fabric, Forge и NeoForge. Quilt тоже должен работать.

**В**: Какие версии Minecraft поддерживаются/поддерживались?  
**А**: Смотрите секцию "[Версии](#версии)".

**В**: Зачем поддерживать столько версий Minecraft?  
**А**: Потому что я могу.

**В**: Нужно ли мне ставить Fabric API или Quilt Standard Libraries?  
**А**: Да, вам нужен Fabric API для Fabric или QFAPI/QSL для Quilt.
Очевидно, что они не нужны для Forge или NeoForge.

**В**: Этот мод нужно ставить на клиент или на сервер?  
**А**: Только на клиент. Серверной версии не существует.

**В**: Это чит?  
**А**: Зависит от вашего мнения на тему "что есть чит?". Этот мод уменьшает
роль, которую играет пинг (задержка) в сражениях с кристаллами. Если точнее,
то он влияет на скорость удаления кристаллов. На установку кристаллов мод НЕ
влияет, так как не существует честного (не читерского) способа сделать это.

**В**: Это задумывалось как чит?  
**А**: Нет.

**В**: Я нашёл баг.  
**А**: Отправляйте все баги [сюда](https://github.com/VidTu/HCsCR/issues) (на
английском языке). Если вы не уверены, баг это или нет, вы можете зайти в
[Discord](https://discord.gg/Q6saSVSuYQ). На уязвимости в моде можно
пожаловаться [сюда](https://github.com/VidTu/HCsCR/security).

**В**: Можно я закину это в свою сборку?  
**А**: Конечно. За упоминание (например, ссылкой на GitHub-страницу мода)
будем премного благодарны, но это необязательно. Монетизация и
распространение модпака разрешены на условиях
[Apache 2.0 License](https://github.com/VidTu/HCsCR/blob/main/LICENSE).
*НА ЗАМЕТКУ*: Некоторые люди могут посчитать этот мод читерским.

**В**: Почему этот мод не на Modrinth или CurseForge?  
**А**: Modrinth
[говорит](https://github.com/user-attachments/assets/437df1a1-3331-499c-ac49-6ec114494bd4),
что это нарушает их [правила](https://modrinth.com/legal/rules). У CurseForge
максимально неприятный API и я не хочу с ним разбираться.

**В**: Зачем нужен этот мод, когда есть серверные кристалл-оптимайзеры?  
**А**: Во-первых, этот мод был создан 2023, задолго до серверных оптимайзеров.
Во-вторых, не на каждом сервере есть оптимайзер. В-третьих, я обновил его в
2025, чтобы выучить [Stonecutter](https://stonecutter.kikugie.dev/).
Теперь его легко обновлять, так что почему бы и нет.

**В**: Насколько быстро он спамит?  
**А**: Достаточно быстро. Точных цифр нет, зависит от пинга, лагов сервера и пр.

<details>
<summary>Абсолютно реальные™ отзывы от счастливых пользователей</summary>
<img alt="Профессионалы® в СНГ сообществе любят мод не просто так" src=https://github.com/VidTu/HCsCR/raw/main/docs/totally_legit_review_ru.png>
</details>

###### Не забудьте посмотреть [Developer FAQ](https://github.com/VidTu/HCsCR/blob/main/docs/CONTRIBUTING.md#developer-faq) для частых вопросов по внутреннему фукнционированию мода. (на английском языке)

## Лицензия

Этот мод предоставляется под лицензией Apache 2.0 License. Посмотрите файлы
[NOTICE](https://github.com/VidTu/HCsCR/blob/main/NOTICE) и
[LICENSE](https://github.com/VidTu/HCsCR/blob/main/LICENSE)
для подробностей. (на английском языке)

## Благодарности

В основном этот мод делает [VidTu](https://github.com/VidTu),
но это было бы невозможно, если бы не:

- [Контрибьюторы](https://github.com/VidTu/HCsCR/graphs/contributors).
- [Stonecutter](https://codeberg.org/stonecutter/stonecutter) от
  [KikuGie](https://codeberg.org/KikuGie). (и контрибьюторов)
- [Blossom](https://github.com/KyoriPowered/blossom) от
  [Kyori](https://github.com/KyoriPowered). (и контрибьюторов)
- [Fabric Loader](https://github.com/FabricMC/fabric-loader),
  [Fabric API](https://github.com/FabricMC/fabric) и
  [Fabric Loom](https://github.com/FabricMC/fabric-loom) от
  [FabricMC](https://github.com/FabricMC). (и контрибьюторов)
- [NeoForge](https://github.com/neoforged/NeoForge),
  [NeoGradle](https://github.com/neoforged/NeoGradle) и
  [ModDevGradle](https://github.com/neoforged/ModDevGradle) от
  [NeoForged](https://github.com/neoforged). (и контрибьюторов)
- [Forge](https://github.com/MinecraftForge/MinecraftForge),
  [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle) и
  [renamer](https://github.com/MinecraftForge/renamer) от
  [Minecraft Forge](https://github.com/MinecraftForge). (и контрибьюторов)
- [Mod Menu](https://github.com/TerraformersMC/ModMenu) от
  [TerraformersMC](https://github.com/TerraformersMC). (и контрибьюторов)
- [Mixin](https://github.com/SpongePowered/Mixin) от
  [SpongePowered](https://github.com/SpongePowered). (и контрибьюторов)
- [MixinExtras](https://github.com/LlamaLad7/MixinExtras) от
  [LlamaLad7](https://github.com/LlamaLad7). (и контрибьюторов)
- [Minecraft](https://minecraft.net/) от
  [Mojang](https://mojang.com/).
- Разные CLI инструменты (`cat, curl, grep, jq, mktemp, sed, sh, tr`) для
  [upload](https://github.com/VidTu/HCsCR/blob/main/dev/upload)-скрипта.

Используются [Gradle](https://gradle.org/) и [Java](https://java.com/).

## Разработка

Загляните в [Dev's Corner](https://github.com/VidTu/HCsCR/blob/main/docs/CONTRIBUTING_ru.md).
(на английском языке)
