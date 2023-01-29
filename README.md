<div style="text-align: center;">
<img src="https://lehtodigital.fi/f/j62zZ" width="400">
</div>

**Aquamarine** is an open source ticket plugin for Spigot servers.

## Features
- 📙 MySQL/MariaDB support (but also flat-file, if you want it simple)
- ✅ Simple permissions
- 🚫 Configurable limits for open tickets per player or inside a radius
- 📝 GUI and commands - easy access for everyone
- 💬 Discord webhook support

## Installation
1. [Download](https://git.lehtodigital.fi:2443/xeno-open-source/aquamarine/-/releases) the .jar file 
2. Move the .jar file to your server's plugins folder
3. Restart your server
4. Configure the plugin on `plugins/aquamarine/config.yml` (see details further down)
5. Restart your server again

## Commands

### Player commands
- `/ticket [message]`
  - Will open a new ticket in the current location
  - Aliases: `/apupyyntö`, `/avunpyyntö`, `/tiketti`, `/helpop`


- `/tickets`
  - Shows a list of your recent tickets
  - Aliases: `/apupyynnöt`, `/avunpyynnöt`, `/tiketit`

### Staff commands
- `/xt`
  - Lists unresolved (open) tickets
  - `/xt help`
    - Shows a help index
  - `/xt all` and `/xt all [page]`
    - Lists all tickets (including open and closed tickets)
  - `/xt player [name]` and `/xt player [name] [page]`
    - Lists all tickets created by a player
  - `/xt view [id]`
    - Shows a certain ticket
  - `/xt goto [id]`
    - Teleports you to a certain ticket
  - `/xt solve [id]` and `/xt solve [id] [comment]`
    - Solves (closes) a ticket, with or without a comment (visible to the player)


- `/xti`
  - Shows the ticket GUI



## Permissions
- `aquamarine.staff`
  - Permission to list and solve (close) tickets


- `aquamarine.ticket`
  - Permission to open new tickets


## Configuring

**Storage methods** available are
- `file` (flat-file as JSON in the plugin folder)
- `mysql` (MySQL/MariaDB, configure the connection below), or
- `memory` (**not persistent** - will not save anything)

```yaml
storage-method: "file"
```

------

**Database connection** - configure your MySQL/MariaDB connection
if you use `mysql` as your storage method.
Otherwise these settings can be ignored.

```yaml
mysql-host: localhost
mysql-port: 3306
mysql-user: user
mysql-pass: pass
mysql-db: database
mysql-table: aquamarine_tickets
```

------

**Max tickets per radius** - this setting will limit the ability to create tickets 
close to each other (inside a radius). For example, with the settings below,
only 3 tickets can be opened simultaneously within a 5 block radius.

```yaml
enable-max-per-radius: true
check-radius: 5.0
max-per-radius: 3
```

------

**Max tickets per player** - this setting will limit the player's ability
to create tickets while old ones have not been resolved.


```yaml
enable-max-per-player: true
max-per-player: 3
```

------

**Message prefix** - used in the beginning of every system message sent by this plugin


```yaml
message-prefix: "&8[&bAquamarine&8] &7"
```

------

**Date format** - used when displaying dates and times
in the GUI and the chat view of tickets.
Uses [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html).

```yaml
date-format: "dd.MM.yyyy kk:mm:ss"   # (SimpleDateFormat)
```

------

**Join announcement** - when a player with admin/moderator permissions joins,
they will be notified about unresolved tickets.
Set the delay of the announcement in seconds.

```yaml
enable-join-announce: true
join-announce-delay-seconds: 5
```

------

**Discord webhook** - enable this and add a webhook URL to receive
announcements about new tickets to your Discord server.

```yaml
enable-webhook: false
webhook-url: https://example.com/
```


## In English?
The plugin's built-in language file is written in Finnish.
Copy and paste the following to the `lang.yml` file to use the plugin in English:

```yaml
# Language file for XAquamarine
generic-no-permission: "You don't have the permission to do that."
generic-not-player: "This command only works for players."
generic-invalid-number: "Invalid number: &b$n$"
generic-ticket-not-found: "The ticket was not found."
generic-invalid-ticket: "Invalid ticket or ticket world. Are all worlds loaded?"
generic-invalid-player: "No such player '$player$'. Are you sure that they have played here with that name?"
generic-no-tickets: "No tickets were found."
generic-previous: "Previous"
generic-next: "Next"
generic-page: "Page "

gui-click-teleport: "Left-click to teleport"
gui-click-solve: "Right-click to resolve"

command-ticket-usage: "Usage: &b/$label$ [message]"
command-ticket-goto-usage: "Usage: &b/$label$ goto [id]"
command-ticket-goto-teleport: "Woosh! And here we are."
command-ticket-solve-usage: "Usage: &b/$label$ solve [id] [optional message]"
command-ticket-solved: "&b$solver$ &7resolved the ticket &b#$n$&7: &f&o$comment$"
command-ticket-view-usage: "Usage: &b/$label$ view [id]"
command-ticket-player-usage: "Usage: &b/$label$ player [player] [page]"

ticket-deny-nearby: "You may not open a new ticket here, as several tickets have already been opened nearby."
ticket-deny-player: "You may not open a new ticket, as you already have several unresolved tickets waiting."

ticket-created: "You opened a new ticket. Number: &b$ticketId$"
ticket-created-announcement: "&b$player$ &7opened a new ticket &b#$ticketId$&7:"
ticket-join-announcement: "There are &b$ticketCount$ unsolved tickets waiting"

ticket-preview-teleport: "Teleport"
ticket-preview-solve: "Mark as resolved"

ticket-hover-title: "&bTicket #$ticketId$"
ticket-hover-sender: "&7Sent by: &f%s"
ticket-hover-timestamp: "&7Timestamp: &f%s"
ticket-hover-location: "&7Location: &f%s"

ticket-hover-solver: "&7Solved by: &f%s"
ticket-hover-solved-at: "&7Solved: &f%s"
ticket-hover-comment: "&7Comment:"
```