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

###App screenshots
![Alt text](https://cloud.githubusercontent.com/assets/7348020/7995639/e7e10f5a-0ae5-11e5-898a-7eead6ea27f1.png)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/7995640/e96adfd6-0ae5-11e5-8131-3e59904bfc0d.png)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/8032816/46322436-0da7-11e5-811a-834b8017d6a2.jpg)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/8032818/4634c56a-0da7-11e5-84c3-1e2b82151749.png)
![Alt text](https://cloud.githubusercontent.com/assets/7348020/8032817/463290a6-0da7-11e5-9660-e82f8302ff51.jpg)



