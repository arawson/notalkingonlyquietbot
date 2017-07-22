#notalkingonlyquietbot: Version 1.0.0#

##Description##
A Discord bot you must host yourself. That's right, you can run this bot on your own hardware, with your own bandwidth, your own API key, and be fully responsible for how your own server uses it.

##Installation:##
1. Clone project.
2. Create a discord application on your discord account: https://discordapp.com/developers/applications/me#top
3. Create a bot user on the application.
4. Build project by issuing `gradle build`
5. Create the distribution jar by running `gradle dist`
6. Copy the distribution jar and run scripts to whatever directory you want
7. Copy the config.toml file into the root of the distribution directory
8. Enter the configuration items you need into the config.toml file (hint: this includes your google api token and your discord api token and client id)
9. Run the bin/notalkingonlyquietbot script to make it run.
10. Add the bot to your server by filling in your client id on this URL: `https://discordapp.com/oauth2/authorize?&client_id=<CLIENT ID>&scope=bot&permissions=0`

##Design Items & Development Progress##
|# |Item |% Complete |Notes |

| ---  | ---  | --:  | ---  |
| ---- | ---- | ---- | ---- |
|      |      |      |      |

###Current Quick TODO:###
|# |Item |
|--- |--- |

##License:##
Licensed under the Apache 2.0 license.

A copy of the license is provided in the accompanying file: `LICENSE.txt`

##Credits:##

- Aaron Rawson: Initial Project Creator
- Donation-begging bots for having poor performance. Prompting the creation of an open-source, hostable, bot.