# spotify-streamer
Get artists by search from spotify and display top tracks for selected artist

##Simple app to search for artists from spotify api and display results and also top tracks of the artists.
## Music player to play preview top tracks of an artist.

###Activities:
#####MainActivity
######For Phone:Adds and removes artists fragment,tracks fragment and trackPreview fragment according to the user's interaction with the app.
######For tablet:Follows a master,detail UI flow for artists fragment and tracks fragment.DialogFragment is used to show the track preview.

###Services:
#####PlayMusicService: Is  used to play selected song.UI elements and service will communicate via binder and messages. 


###Apis used.
#####1.Picasso api to download the image.
#####2.Spotify api for android to get the results of artists and artist tracks.
#####3.Media Player api to play songs

###Working video
https://www.youtube.com/watch?v=WL8nIPVCgMQ

###App screenshots
![screenshot_2015-06-11-22-10-09](https://cloud.githubusercontent.com/assets/7348020/8122398/4df07578-1087-11e5-98e9-db19cb502066.png)
![screenshot_2015-06-11-22-10-14](https://cloud.githubusercontent.com/assets/7348020/8122399/4f39531e-1087-11e5-8537-cf4ceba07322.png)
![screenshot_2015-06-11-22-10-21](https://cloud.githubusercontent.com/assets/7348020/8122400/50667e74-1087-11e5-8a56-55a11a31f7aa.png)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/8032816/46322436-0da7-11e5-811a-834b8017d6a2.jpg)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/8032817/463290a6-0da7-11e5-9660-e82f8302ff51.jpg)



