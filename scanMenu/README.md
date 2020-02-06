# Basic Git Commands

```
git clone                           //Copy and initialize a repo
git init                            //Initialize git repo
git remote add origin               //Add repository http
git add -A                          //Stage all changes
git commit -m "msg"                 //Commit staged changes
git pull --rebase origin master     //Sync with remote repo
git push origin master              //Push changes
```

## Clone this repo:
```
git clone https://github.com/devYaoYH/scanMenu.git
```

## Git Terminology
Repository - Shared team codebase
Remote - Code that is uploaded to the repo
Local - Code that you have on your machine

## Managing Merge Conflicts

We'll be using [Meld](http://meldmerge.org/). See [StackOverflow](https://stackoverflow.com/questions/34119866/setting-up-and-using-meld-as-your-git-difftool-and-mergetool) for setup instructions.

## Workflow

```
//Write Code :D
git add -A                          //Add all your changes
git commit -m "Your Update Message" //Commit your code!
git pull --rebase origin master     //This tells git to apply your local commits OVER the remote branch
//If we have merge conflicts, run the next 2 lines
git mergetool                       //See above ^use Meld tool
git rebase --continue               //After we resolve conflicts, allow update to continue
git push origin master              //Update our shared repository with your changes
```